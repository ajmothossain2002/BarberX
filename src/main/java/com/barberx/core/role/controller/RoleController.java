package com.barberx.core.role.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.role.dto.RoleDto;
import com.barberx.core.role.service.RoleService;
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
 * Role management controller – exposes role listing APIs.
 * Read access requires ADMIN role.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all roles",
            description = "Returns a list of all active roles with their permissions. Requires ADMIN role."
    )
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRolesDto();
        return ResponseEntity.ok(ApiResponse.success(roles, "Roles retrieved successfully"));
    }
}
