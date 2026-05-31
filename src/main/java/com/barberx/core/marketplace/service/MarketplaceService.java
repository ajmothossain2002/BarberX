package com.barberx.core.marketplace.service;

import com.barberx.core.shop.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for public customer marketplace operations.
 */
public interface MarketplaceService {

    /**
     * Search and list verified (APPROVED), non-deleted shops with paginated and filterable options.
     */
    Page<ShopResponse> searchShops(String search, String businessType, String city, Pageable pageable);

    /**
     * Retrieves the details of a verified, non-deleted shop.
     */
    ShopResponse getShopDetails(Long id);
}
