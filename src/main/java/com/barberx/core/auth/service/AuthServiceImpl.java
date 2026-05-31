package com.barberx.core.auth.service;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.security.JwtUtil;
import com.barberx.common.exception.CustomException;
import com.barberx.core.auth.dto.request.LoginRequest;
import com.barberx.core.auth.dto.request.RegisterRequest;
import com.barberx.core.auth.dto.response.AuthResponse;
import com.barberx.core.auth.validation.AuthValidationService;
import com.barberx.core.role.entity.Role;
import com.barberx.core.role.validation.RoleValidationService;
import com.barberx.core.user.entity.CustomerProfile;
import com.barberx.core.user.entity.ShopOwnerProfile;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.mapper.UserMapper;
import com.barberx.core.user.repository.CustomerProfileRepository;
import com.barberx.core.user.repository.ShopOwnerProfileRepository;
import com.barberx.core.user.repository.UserRepository;
import com.barberx.core.user.validation.UserValidationService;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Authentication service implementation handling registration and login flows.
 * Calculates nextRoute, role, and profileCompleted values dynamically to centralize routing rules.
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
    private final CustomerProfileRepository customerProfileRepository;
    private final ShopOwnerProfileRepository shopOwnerProfileRepository;
    private final ShopRepository shopRepository;

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

        // Step 4: Create appropriate Profile depending on the role
        if (role.getName().equalsIgnoreCase(AppConstants.ROLE_USER)) {
            CustomerProfile customerProfile = CustomerProfile.builder()
                    .user(user)
                    .fullName(user.getFullName())
                    .phone(user.getPhoneNumber())
                    .build();
            customerProfileRepository.save(customerProfile);
        } else if (role.getName().equalsIgnoreCase(AppConstants.ROLE_OWNER)) {
            ShopOwnerProfile shopOwnerProfile = ShopOwnerProfile.builder()
                    .user(user)
                    .ownerName(user.getFullName())
                    .phone(user.getPhoneNumber())
                    .profileCompleted(false)
                    .build();
            shopOwnerProfileRepository.save(shopOwnerProfile);
        }

        // Step 5: Generate JWT and build response with dynamic routing
        List<String> roleNames = List.of(role.getName().toUpperCase());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roleNames);

        AuthRoutingInfo routingInfo = resolveRouting(user, roleNames);

        return AuthResponse.builder()
                .accessToken(token)
                .role(routingInfo.role)
                .profileCompleted(routingInfo.profileCompleted)
                .shopId(routingInfo.shopId)
                .nextRoute(routingInfo.nextRoute)
                .user(UserMapper.toUserInfo(user, routingInfo.profileCompleted))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        // Step 1: Validate credentials and active status
        User user = authValidationService.validateLoginCredentials(
                request.getEmail(), request.getPassword());

        // Step 2: Generate JWT with user roles
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();

        // Step 3: Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roleNames);
        log.info("User logged in successfully: {}", user.getEmail());

        // Step 4: Resolve nextRoute, profileCompleted and shopId
        AuthRoutingInfo routingInfo = resolveRouting(user, roleNames);

        return AuthResponse.builder()
                .accessToken(token)
                .role(routingInfo.role)
                .profileCompleted(routingInfo.profileCompleted)
                .shopId(routingInfo.shopId)
                .nextRoute(routingInfo.nextRoute)
                .user(UserMapper.toUserInfo(user, routingInfo.profileCompleted))
                .build();
    }

    /**
     * Resolves the authoritative next route, profile completion status, and shop ID for a user.
     */
    private AuthRoutingInfo resolveRouting(User user, List<String> roleNames) {
        String primaryRole = AppConstants.ROLE_USER;
        if (roleNames.contains(AppConstants.ROLE_ADMIN)) {
            primaryRole = AppConstants.ROLE_ADMIN;
        } else if (roleNames.contains(AppConstants.ROLE_OWNER)) {
            primaryRole = AppConstants.ROLE_OWNER;
        } else if (roleNames.contains(AppConstants.ROLE_BARBER)) {
            primaryRole = AppConstants.ROLE_BARBER;
        }

        Boolean profileCompleted = true;
        Long shopId = null;
        String nextRoute = "/customer/home";

        if (primaryRole.equals(AppConstants.ROLE_ADMIN)) {
            nextRoute = "/admin";
            profileCompleted = true;
        } else if (primaryRole.equals(AppConstants.ROLE_OWNER)) {
            ShopOwnerProfile ownerProfile = shopOwnerProfileRepository.findByUserIdAndDeletedFalse(user.getId())
                    .orElse(null);
            
            if (ownerProfile == null) {
                profileCompleted = false;
                nextRoute = "/owner/setup";
            } else {
                profileCompleted = ownerProfile.isProfileCompleted();
                // Verify shop exists and links to this owner profile
                List<Shop> shops = shopRepository.findByOwnerIdAndDeletedFalse(user.getId());
                if (!shops.isEmpty()) {
                    shopId = shops.get(0).getId();
                    profileCompleted = true; // authoritative status override

                    // Sync database structure: ensure shop owner_profile_id index exists correctly
                    for (Shop s : shops) {
                        if (s.getOwnerProfile() == null) {
                            s.setOwnerProfile(ownerProfile);
                            shopRepository.save(s);
                        }
                    }
                } else {
                    profileCompleted = false;
                }

                if (profileCompleted) {
                    nextRoute = "/owner/dashboard";
                } else {
                    nextRoute = "/owner/setup";
                }
            }
        }

        return new AuthRoutingInfo(primaryRole, profileCompleted, shopId, nextRoute);
    }

    private static class AuthRoutingInfo {
        final String role;
        final Boolean profileCompleted;
        final Long shopId;
        final String nextRoute;

        AuthRoutingInfo(String role, Boolean profileCompleted, Long shopId, String nextRoute) {
            this.role = role;
            this.profileCompleted = profileCompleted;
            this.shopId = shopId;
            this.nextRoute = nextRoute;
        }
    }
}
