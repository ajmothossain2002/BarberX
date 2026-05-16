package com.barberx.core.user.service;

import com.barberx.core.user.entity.User;

import java.util.Optional;

/**
 * User service contract for user management operations.
 */
public interface UserService {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
