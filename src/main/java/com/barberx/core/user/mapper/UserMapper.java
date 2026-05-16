package com.barberx.core.user.mapper;

import com.barberx.core.auth.dto.response.AuthResponse;
import com.barberx.core.user.entity.User;

import java.util.List;

/**
 * Stateless mapper utility for converting User entities to DTOs.
 */
public final class UserMapper {

    private UserMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a User entity to an AuthResponse DTO.
     */
    public static AuthResponse.UserInfo toUserInfo(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(roleNames)
                .build();
    }
}
