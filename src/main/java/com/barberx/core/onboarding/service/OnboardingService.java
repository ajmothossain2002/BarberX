package com.barberx.core.onboarding.service;

import com.barberx.core.onboarding.dto.OnboardingRequest;
import com.barberx.core.shop.dto.response.ShopResponse;

/**
 * Service interface for shop owner business onboarding operations.
 */
public interface OnboardingService {

    /**
     * Submits wizard onboarding details, creates the corresponding Shop entity
     * with its schedule, and sets profileCompleted to true.
     */
    ShopResponse completeOnboarding(OnboardingRequest request);
}
