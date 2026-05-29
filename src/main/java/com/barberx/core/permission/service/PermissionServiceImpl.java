package com.barberx.core.permission.service;

import com.barberx.core.permission.dto.PermissionDto;
import com.barberx.core.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for permission management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        log.debug("Fetching all active permissions");
        return permissionRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .map(p -> PermissionDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .module(p.getModule())
                        .build())
                .toList();
    }
}
