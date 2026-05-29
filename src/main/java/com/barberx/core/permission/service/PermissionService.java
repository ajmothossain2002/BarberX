package com.barberx.core.permission.service;

import com.barberx.core.permission.dto.PermissionDto;

import java.util.List;

/**
 * Permission service contract for permission management operations.
 */
public interface PermissionService {

    /**
     * Returns all non-deleted permissions as DTOs.
     */
    List<PermissionDto> getAllPermissions();
}
