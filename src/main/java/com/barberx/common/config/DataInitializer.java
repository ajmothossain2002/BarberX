package com.barberx.common.config;

import com.barberx.common.constants.AppConstants;
import com.barberx.core.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes required seed data on application startup.
 * Creates default roles if they don't already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) {
        log.info("Initializing default roles...");
        roleService.createRoleIfNotExists(AppConstants.ROLE_ADMIN, "System Administrator");
        roleService.createRoleIfNotExists(AppConstants.ROLE_USER, "Standard User");
        roleService.createRoleIfNotExists(AppConstants.ROLE_BARBER, "Barber / Service Provider");
        roleService.createRoleIfNotExists(AppConstants.ROLE_OWNER, "Shop Owner");
        log.info("Default roles initialized successfully");
    }
}
