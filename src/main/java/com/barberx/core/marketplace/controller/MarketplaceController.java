package com.barberx.core.marketplace.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.marketplace.service.MarketplaceService;
import com.barberx.core.shop.dto.response.ShopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing public endpoints for the salon/barber marketplace.
 * Read-only APIs. Paths are marked public in security configuration.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/marketplace")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Public-facing search and profile discovery APIs for customers")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    @GetMapping("/shops")
    @Operation(
            summary = "Search marketplace shops",
            description = "Returns a paginated list of verified approved shops. Allows filtering by business type, city, and general text search."
    )
    public ResponseEntity<ApiResponse<Page<ShopResponse>>> searchShops(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ShopResponse> shops = marketplaceService.searchShops(search, businessType, city, pageable);
        return ResponseEntity.ok(ApiResponse.success(shops, "Marketplace shops retrieved successfully"));
    }

    @GetMapping("/shops/{id}")
    @Operation(
            summary = "Get marketplace shop details",
            description = "Returns complete profile details and operating hours for a verified shop."
    )
    public ResponseEntity<ApiResponse<ShopResponse>> getShopDetails(
            @PathVariable Long id) {
        ShopResponse shop = marketplaceService.getShopDetails(id);
        return ResponseEntity.ok(ApiResponse.success(shop, "Marketplace shop details retrieved successfully"));
    }
}
