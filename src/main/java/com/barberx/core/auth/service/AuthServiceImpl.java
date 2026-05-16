package com.barberx.core.auth.service;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.security.JwtUtil;
import com.barberx.core.auth.dto.request.LoginRequest;
import com.barberx.core.auth.dto.request.RegisterRequest;
import com.barberx.core.auth.dto.response.AuthResponse;
import com.barberx.core.auth.validation.AuthValidationService;
import com.barberx.core.role.entity.Role;
import com.barberx.core.role.validation.RoleValidationService;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.mapper.UserMapper;
import com.barberx.core.user.repository.UserRepository;
import com.barberx.core.user.validation.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Authentication service implementation handling registration and login flows.
 * Delegates validation to dedicated validation services (ERP pattern).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserValidationService userValidationService;
    private final RoleValidationService roleValidationService;
    private final AuthValidationService authValidationService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Step 1: Validate uniqueness constraints
        userValidationService.validateRegistrationUniqueness(
                request.getEmail(), request.getPhoneNumber());

        // Step 2: Validate and resolve role
        String roleName = (request.getRoleName() != null && !request.getRoleName().isBlank())
                ? request.getRoleName()
                : AppConstants.ROLE_USER;
        Role role = roleValidationService.validateAndGetRole(roleName);

        // Step 3: Build and persist user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        // Step 4: Generate JWT and build response
        List<String> roleNames = List.of(role.getName().toUpperCase());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roleNames);

        return AuthResponse.builder()
                .accessToken(token)
                .user(UserMapper.toUserInfo(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        // Step 1: Validate credentials and active status
        User user = authValidationService.validateLoginCredentials(
                request.getEmail(), request.getPassword());

        // Step 2: Generate JWT with user roles
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roleNames);
        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .user(UserMapper.toUserInfo(user))
                .build();
    }
}
