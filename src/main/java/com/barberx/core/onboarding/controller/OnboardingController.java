package com.barberx.core.onboarding.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.onboarding.dto.OnboardingRequest;
import com.barberx.core.onboarding.service.OnboardingService;
import com.barberx.core.shop.dto.response.ShopResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for completing the Shop Owner business onboarding wizard.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/onboarding")
@RequiredArgsConstructor
@Tag(name = "Business Onboarding", description = "APIs for completing the shop owner setup wizard")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(
            summary = "Complete business onboarding",
            description = "Saves owner info, creates a shop with coordinates/business details, seeds opening hours, and marks onboarding complete."
    )
    public ResponseEntity<ApiResponse<ShopResponse>> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request) {
        ShopResponse shop = onboardingService.completeOnboarding(request);
        return ResponseEntity.ok(ApiResponse.success(shop, "Onboarding setup completed successfully"));
    }
}
