package com.barberx.core.auth.service;

import com.barberx.core.auth.dto.request.LoginRequest;
import com.barberx.core.auth.dto.request.RegisterRequest;
import com.barberx.core.auth.dto.response.AuthResponse;

/**
 * Authentication service contract for register and login operations.
 */
public interface AuthService {

    /**
     * Registers a new user, assigns a role, and returns JWT credentials.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with credentials and returns JWT credentials.
     */
    AuthResponse login(LoginRequest request);
}
