package com.barberx.core.user.service;

import com.barberx.common.exception.CustomException;
import com.barberx.common.util.UserContextUtil;
import com.barberx.core.role.entity.Role;
import com.barberx.core.role.validation.RoleValidationService;
import com.barberx.core.user.dto.UserDto;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.enums.UserStatus;
import com.barberx.core.user.mapper.UserMapper;
import com.barberx.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service implementation for user management operations.
 * Follows the MVC Service layer pattern – orchestrates repository calls,
 * delegates validation to dedicated validation services, and maps to DTOs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleValidationService roleValidationService;

    // ─── Existing Auth-Level Methods ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id);
    }

    // ─── Admin / Management CRUD Methods ──────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all active users");
        return userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = findActiveUserOrThrow(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long userId, String roleName) {
        log.info("Updating role for user {} to {}", userId, roleName);

        User user = findActiveUserOrThrow(userId);
        Role role = roleValidationService.validateAndGetRole(roleName);

        user.getRoles().clear();
        user.getRoles().add(role);
        user = userRepository.save(user);

        log.info("Role updated successfully for user {}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(Long userId, UserStatus status) {
        log.info("Updating status for user {} to {}", userId, status);

        User user = findActiveUserOrThrow(userId);
        user.setStatus(status);
        user = userRepository.save(user);

        log.info("Status updated successfully for user {}", userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Soft-deleting user {}", userId);

        User user = findActiveUserOrThrow(userId);
        String currentUser = UserContextUtil.getCurrentUserEmail();
        user.softDelete(currentUser);
        userRepository.save(user);

        log.info("User {} soft-deleted by {}", userId, currentUser);
    }

    // ─── Private Helpers ──────────────────────────────────────────────

    /**
     * Retrieves an active (non-deleted) user or throws a 404 exception.
     */
    private User findActiveUserOrThrow(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "User not found with ID: " + id, HttpStatus.NOT_FOUND));
    }
}

