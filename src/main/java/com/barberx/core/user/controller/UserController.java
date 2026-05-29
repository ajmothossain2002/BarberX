package com.barberx.core.user.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.user.dto.UpdateUserRoleRequest;
import com.barberx.core.user.dto.UpdateUserStatusRequest;
import com.barberx.core.user.dto.UserDto;
import com.barberx.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller – Admin-only endpoints for viewing, updating, and deleting users.
 * Follows the MVC Controller pattern: thin controller delegating all business logic to UserService.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin APIs for managing user accounts")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users",
            description = "Returns a list of all active user accounts. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Returns details for a specific user. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user role",
            description = "Replaces the user's current role with the specified role. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserDto updated = userService.updateUserRole(id, request.getRoleName());
        return ResponseEntity.ok(ApiResponse.success(updated, "User role updated successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user status",
            description = "Changes the user's account status (ACTIVE, INACTIVE, SUSPENDED). Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserDto updated = userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(updated, "User status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user",
            description = "Soft-deletes a user account. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
