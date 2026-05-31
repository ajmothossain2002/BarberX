package com.barberx.core.shop.service;

import com.barberx.common.exception.CustomException;
import com.barberx.common.util.UserContextUtil;
import com.barberx.core.shop.dto.request.*;
import com.barberx.core.shop.dto.response.ShopHoursResponse;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.entity.ShopHours;
import com.barberx.core.shop.enums.VerificationStatus;
import com.barberx.core.shop.mapper.ShopMapper;
import com.barberx.core.shop.repository.ShopHoursRepository;
import com.barberx.core.shop.repository.ShopRepository;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.entity.ShopOwnerProfile;
import com.barberx.core.user.repository.UserRepository;
import com.barberx.core.user.repository.ShopOwnerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shop service implementation — handles all shop CRUD, hours management,
 * ownership enforcement, and admin verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ShopHoursRepository shopHoursRepository;
    private final UserRepository userRepository;
    private final ShopOwnerProfileRepository shopOwnerProfileRepository;

    // ─── Owner Operations ──────────────────────────────────────────────

    @Override
    @Transactional
    public ShopResponse createShop(CreateShopRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("Creating shop '{}' for user {}", request.getName(), currentUserId);

        User owner = userRepository.findByIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        ShopOwnerProfile ownerProfile = shopOwnerProfileRepository.findByUserIdAndDeletedFalse(currentUserId)
                .orElse(null);

        Shop shop = ShopMapper.toEntity(request, owner, ownerProfile);
        shop = shopRepository.save(shop);

        log.info("Shop created successfully with ID {}", shop.getId());
        return ShopMapper.toResponse(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopById(Long id) {
        Shop shop = findActiveShopOrThrow(id);
        return ShopMapper.toResponse(shop, true);
    }

    @Override
    @Transactional
    public ShopResponse updateShop(Long id, UpdateShopRequest request) {
        Shop shop = findActiveShopOrThrow(id);
        enforceOwnership(shop);

        log.info("Updating shop {} by user {}", id, getCurrentUserId());
        ShopMapper.updateEntity(shop, request);
        shop = shopRepository.save(shop);

        log.info("Shop {} updated successfully", id);
        return ShopMapper.toResponse(shop);
    }

    @Override
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = findActiveShopOrThrow(id);
        enforceOwnership(shop);

        log.info("Soft-deleting shop {} by user {}", id, getCurrentUserId());
        String currentUser = UserContextUtil.getCurrentUserEmail();
        shop.softDelete(currentUser);
        shopRepository.save(shop);

        log.info("Shop {} soft-deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getMyShops() {
        Long currentUserId = getCurrentUserId();
        log.debug("Fetching shops for owner {}", currentUserId);

        return shopRepository.findByOwnerIdAndDeletedFalse(currentUserId).stream()
                .map(ShopMapper::toResponse)
                .toList();
    }

    // ─── Hours Operations ──────────────────────────────────────────────

    @Override
    @Transactional
    public List<ShopHoursResponse> updateShopHours(Long shopId, ShopHoursRequest request) {
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        log.info("Replacing shop hours for shop {} ({} entries)", shopId, request.getHours().size());

        // Clear existing hours (orphanRemoval handles DB deletes)
        shop.getShopHours().clear();

        // Build new hours
        List<ShopHours> newHours = new ArrayList<>();
        for (ShopHoursRequest.ShopHourEntry entry : request.getHours()) {
            ShopHours hours = ShopHours.builder()
                    .shop(shop)
                    .dayOfWeek(entry.getDayOfWeek().toUpperCase())
                    .openingTime(parseTime(entry.getOpeningTime()))
                    .closingTime(parseTime(entry.getClosingTime()))
                    .isHoliday(entry.isHoliday())
                    .build();
            newHours.add(hours);
        }

        shop.getShopHours().addAll(newHours);
        shopRepository.save(shop);

        log.info("Shop hours updated successfully for shop {}", shopId);
        return newHours.stream()
                .map(ShopMapper::toHoursResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopHoursResponse> getShopHours(Long shopId) {
        // Verify shop exists
        findActiveShopOrThrow(shopId);

        return shopHoursRepository.findByShopIdAndIsDeletedFalse(shopId).stream()
                .map(ShopMapper::toHoursResponse)
                .toList();
    }

    // ─── Admin Operations ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShops(Pageable pageable, VerificationStatus status) {
        log.debug("Admin fetching shops — status filter: {}", status);

        Page<Shop> shops;
        if (status != null) {
            shops = shopRepository.findAllByVerificationStatusAndDeletedFalse(status, pageable);
        } else {
            shops = shopRepository.findAllByDeletedFalse(pageable);
        }

        return shops.map(ShopMapper::toResponse);
    }

    @Override
    @Transactional
    public ShopResponse verifyShop(Long id, VerifyShopRequest request) {
        Shop shop = findActiveShopOrThrow(id);

        log.info("Admin verifying shop {} → {}", id, request.getVerificationStatus());
        shop.setVerificationStatus(request.getVerificationStatus());
        shop = shopRepository.save(shop);

        log.info("Shop {} verified as {}", id, request.getVerificationStatus());
        return ShopMapper.toResponse(shop);
    }

    // ─── Private Helpers ───────────────────────────────────────────────

    /**
     * Retrieves an active (non-deleted) shop or throws 404.
     */
    private Shop findActiveShopOrThrow(Long id) {
        return shopRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException(
                        "Shop not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Enforces that the current user is the owner of the given shop.
     */
    private void enforceOwnership(Shop shop) {
        Long currentUserId = getCurrentUserId();
        if (!shop.getOwner().getId().equals(currentUserId)) {
            throw new CustomException("You do not have permission to modify this shop", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Extracts the current user's ID from the thread-local UserContext.
     */
    private Long getCurrentUserId() {
        var context = UserContextUtil.getContext();
        if (context == null || context.getUserId() == null) {
            throw new CustomException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return context.getUserId();
    }

    /**
     * Parses a time string in HH:mm format, returning null for blank/null input.
     */
    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new CustomException("Invalid time format: " + time + ". Expected HH:mm", HttpStatus.BAD_REQUEST);
        }
    }
}
