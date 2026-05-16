package com.barberx.core.user.validation;

import com.barberx.common.exception.CustomException;
import com.barberx.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ERP-style validation service for user-related business rules.
 * Centralizes uniqueness checks and data integrity validations.
 */
@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UserRepository userRepository;

    /**
     * Validates that the email is not already registered.
     */
    public void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new CustomException("Email is already registered: " + email, HttpStatus.CONFLICT);
        }
    }

    /**
     * Validates that the phone number is not already registered.
     */
    public void validatePhoneNumberUniqueness(String phoneNumber) {
        if (userRepository.existsByPhoneNumberAndDeletedFalse(phoneNumber)) {
            throw new CustomException("Phone number is already registered: " + phoneNumber, HttpStatus.CONFLICT);
        }
    }

    /**
     * Validates both email and phone uniqueness for new user registration.
     */
    public void validateRegistrationUniqueness(String email, String phoneNumber) {
        validateEmailUniqueness(email);
        validatePhoneNumberUniqueness(phoneNumber);
    }
}
