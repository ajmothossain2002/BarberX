package com.barberx.core.user.mapper;

import com.barberx.core.auth.dto.response.AuthResponse;
import com.barberx.core.user.dto.UserDto;
import com.barberx.core.user.entity.User;

import java.util.List;

/**
 * Stateless mapper utility for converting User entities to DTOs.
 * Centralises all User ↔ DTO transformations in one place (SRP).
 */
public final class UserMapper {

    private UserMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a User entity to an AuthResponse DTO (used during login/register).
     */
    public static AuthResponse.UserInfo toUserInfo(User user) {
        return toUserInfo(user, null);
    }

    /**
     * Maps a User entity to an AuthResponse DTO (used during login/register) with onboarding completion flag.
     */
    public static AuthResponse.UserInfo toUserInfo(User user, Boolean profileCompleted) {
        List<String> roleNames = extractRoleNames(user);

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(roleNames)
                .profileCompleted(profileCompleted)
                .build();
    }

    /**
     * Maps a User entity to a UserDto (used in admin/management APIs).
     */
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roles(extractRoleNames(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Extracts uppercased role names from the user's role set.
     */
    private static List<String> extractRoleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .toList();
    }
}
