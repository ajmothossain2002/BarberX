package com.barberx.core.permission.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.permission.dto.PermissionDto;
import com.barberx.core.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Permission management controller – exposes permission listing APIs.
 * Read access requires ADMIN role.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "APIs for managing permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all permissions",
            description = "Returns a list of all active permissions. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions, "Permissions retrieved successfully"));
    }
}
