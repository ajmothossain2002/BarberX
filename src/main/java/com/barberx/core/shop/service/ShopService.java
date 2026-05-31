package com.barberx.core.shop.service;

import com.barberx.core.shop.dto.request.*;
import com.barberx.core.shop.dto.response.ShopHoursResponse;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for shop management operations.
 */
public interface ShopService {

    // ─── Owner Operations ──────────────────────────────────────
    ShopResponse createShop(CreateShopRequest request);

    ShopResponse getShopById(Long id);

    ShopResponse updateShop(Long id, UpdateShopRequest request);

    void deleteShop(Long id);

    List<ShopResponse> getMyShops();

    // ─── Hours ─────────────────────────────────────────────────
    List<ShopHoursResponse> updateShopHours(Long shopId, ShopHoursRequest request);

    List<ShopHoursResponse> getShopHours(Long shopId);

    // ─── Admin Operations ──────────────────────────────────────
    Page<ShopResponse> getAllShops(Pageable pageable, VerificationStatus status);

    ShopResponse verifyShop(Long id, VerifyShopRequest request);
}
