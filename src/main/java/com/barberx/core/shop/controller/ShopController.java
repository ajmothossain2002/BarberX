package com.barberx.core.shop.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.shop.dto.request.CreateShopRequest;
import com.barberx.core.shop.dto.request.ShopHoursRequest;
import com.barberx.core.shop.dto.request.UpdateShopRequest;
import com.barberx.core.shop.dto.response.ShopHoursResponse;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shop management controller — OWNER endpoints for managing their own shops.
 * All endpoints require authentication; ownership is enforced in the service layer.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/shops")
@RequiredArgsConstructor
@Tag(name = "Shop Management", description = "APIs for creating and managing barber/salon shops")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new shop", description = "Creates a new shop for the authenticated owner. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<ShopResponse>> createShop(
            @Valid @RequestBody CreateShopRequest request) {
        ShopResponse shop = shopService.createShop(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(shop, "Shop created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shop by ID", description = "Returns shop details including operating hours.")
    public ResponseEntity<ApiResponse<ShopResponse>> getShopById(@PathVariable Long id) {
        ShopResponse shop = shopService.getShopById(id);
        return ResponseEntity.ok(ApiResponse.success(shop, "Shop retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update shop", description = "Updates shop details. Only the shop owner can update.")
    public ResponseEntity<ApiResponse<ShopResponse>> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShopRequest request) {
        ShopResponse shop = shopService.updateShop(id, request);
        return ResponseEntity.ok(ApiResponse.success(shop, "Shop updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete shop", description = "Soft-deletes a shop. Only the shop owner can delete.")
    public ResponseEntity<ApiResponse<Void>> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Shop deleted successfully"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get my shops", description = "Returns all shops owned by the authenticated user.")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getMyShops() {
        List<ShopResponse> shops = shopService.getMyShops();
        return ResponseEntity.ok(ApiResponse.success(shops, "Shops retrieved successfully"));
    }

    // ─── Hours Endpoints ───────────────────────────────────────────

    @PutMapping("/{id}/hours")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update shop hours", description = "Full-replace of shop operating hours for all days.")
    public ResponseEntity<ApiResponse<List<ShopHoursResponse>>> updateShopHours(
            @PathVariable Long id,
            @Valid @RequestBody ShopHoursRequest request) {
        List<ShopHoursResponse> hours = shopService.updateShopHours(id, request);
        return ResponseEntity.ok(ApiResponse.success(hours, "Shop hours updated successfully"));
    }

    @GetMapping("/{id}/hours")
    @Operation(summary = "Get shop hours", description = "Returns operating hours for all days of the week.")
    public ResponseEntity<ApiResponse<List<ShopHoursResponse>>> getShopHours(@PathVariable Long id) {
        List<ShopHoursResponse> hours = shopService.getShopHours(id);
        return ResponseEntity.ok(ApiResponse.success(hours, "Shop hours retrieved successfully"));
    }
}
