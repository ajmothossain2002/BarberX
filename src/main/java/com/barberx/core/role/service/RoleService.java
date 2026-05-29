package com.barberx.core.role.service;

import com.barberx.core.role.dto.RoleDto;
import com.barberx.core.role.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * Role service contract for role management operations.
 */
public interface RoleService {

    Optional<Role> findByName(String name);

    List<Role> findAll();

    Role createRoleIfNotExists(String name, String description);

    /**
     * Returns all non-deleted roles as DTOs.
     */
    List<RoleDto> getAllRolesDto();

    /**
     * Returns a single role by ID.
     */
    Optional<Role> findById(Long id);
}

