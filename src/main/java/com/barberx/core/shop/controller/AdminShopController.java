package com.barberx.core.shop.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.shop.dto.request.VerifyShopRequest;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.enums.VerificationStatus;
import com.barberx.core.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin shop management controller — endpoints for listing all shops and verifying them.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/admin/shops")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Shop Management", description = "Admin APIs for managing and verifying shops")
public class AdminShopController {

    private final ShopService shopService;

    @GetMapping
    @Operation(summary = "List all shops", description = "Returns a paginated list of all shops. Optionally filter by verification status.")
    public ResponseEntity<ApiResponse<Page<ShopResponse>>> getAllShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) VerificationStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShopResponse> shops = shopService.getAllShops(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(shops, "Shops retrieved successfully"));
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify shop", description = "Approve or reject a shop verification request.")
    public ResponseEntity<ApiResponse<ShopResponse>> verifyShop(
            @PathVariable Long id,
            @Valid @RequestBody VerifyShopRequest request) {
        ShopResponse shop = shopService.verifyShop(id, request);
        return ResponseEntity.ok(ApiResponse.success(shop, "Shop verification updated successfully"));
    }
}
