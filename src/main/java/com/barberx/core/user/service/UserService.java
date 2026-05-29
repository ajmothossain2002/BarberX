package com.barberx.core.user.service;

import com.barberx.core.user.dto.UserDto;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.enums.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * User service contract for user management operations.
 * Follows the Service layer pattern – Controllers delegate here,
 * and this layer orchestrates Repository calls and validation.
 */
public interface UserService {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    /**
     * Returns all non-deleted users as DTOs (admin view).
     */
    List<UserDto> getAllUsers();

    /**
     * Returns a single user DTO by ID.
     */
    UserDto getUserById(Long id);

    /**
     * Replaces the user's current roles with the given role.
     */
    UserDto updateUserRole(Long userId, String roleName);

    /**
     * Updates the user's account status (ACTIVE, INACTIVE, SUSPENDED, etc.).
     */
    UserDto updateUserStatus(Long userId, UserStatus status);

    /**
     * Soft-deletes a user by ID.
     */
    void deleteUser(Long userId);
}

