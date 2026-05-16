package com.barberx.core.role.validation;

import com.barberx.common.exception.CustomException;
import com.barberx.core.role.entity.Role;
import com.barberx.core.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ERP-style validation service for role-related business rules.
 */
@Service
@RequiredArgsConstructor
public class RoleValidationService {

    private final RoleRepository roleRepository;

    /**
     * Validates that the role exists and returns it.
     *
     * @throws CustomException if the role is not found
     */
    public Role validateAndGetRole(String roleName) {
        return roleRepository.findByNameIgnoreCaseAndDeletedFalse(roleName)
                .orElseThrow(() -> new CustomException(
                        "Role not found: " + roleName, HttpStatus.BAD_REQUEST));
    }

    /**
     * Validates role name uniqueness for creation.
     */
    public void validateRoleNameUniqueness(String roleName) {
        if (roleRepository.existsByNameIgnoreCaseAndDeletedFalse(roleName)) {
            throw new CustomException("Role already exists: " + roleName, HttpStatus.CONFLICT);
        }
    }
}
