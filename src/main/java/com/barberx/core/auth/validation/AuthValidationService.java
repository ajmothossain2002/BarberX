package com.barberx.core.auth.validation;

import com.barberx.common.exception.CustomException;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * ERP-style validation service for authentication operations.
 * Separates validation logic from business logic for maintainability.
 */
@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates login credentials and returns the authenticated user.
     *
     * @throws CustomException if credentials are invalid or user is inactive
     */
    public User validateLoginCredentials(String email, String rawPassword) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isActive()) {
            throw new CustomException("User account is not active. Please contact support.", HttpStatus.FORBIDDEN);
        }

        return user;
    }
}
