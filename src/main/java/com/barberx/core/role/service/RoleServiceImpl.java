package com.barberx.core.role.service;

import com.barberx.core.role.dto.RoleDto;
import com.barberx.core.role.entity.Role;
import com.barberx.core.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByNameIgnoreCaseAndDeletedFalse(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAllByDeletedFalse();
    }

    @Override
    @Transactional
    public Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByNameIgnoreCaseAndDeletedFalse(name)
                .orElseGet(() -> {
                    log.info("Creating default role: {}", name);
                    Role role = Role.builder()
                            .name(name.toUpperCase())
                            .description(description)
                            .build();
                    return roleRepository.save(role);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRolesDto() {
        return roleRepository.findAllByDeletedFalse().stream()
                .map(role -> RoleDto.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .permissions(role.getPermissions().stream()
                                .map(p -> p.getName())
                                .toList())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id)
                .filter(r -> !r.isDeleted());
    }
}

