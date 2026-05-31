package com.barberx.core.onboarding.service;

import com.barberx.common.exception.CustomException;
import com.barberx.common.util.UserContextUtil;
import com.barberx.core.onboarding.dto.OnboardingRequest;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.entity.ShopHours;
import com.barberx.core.shop.enums.VerificationStatus;
import com.barberx.core.shop.mapper.ShopMapper;
import com.barberx.core.shop.repository.ShopRepository;
import com.barberx.core.user.entity.ShopOwnerProfile;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.repository.ShopOwnerProfileRepository;
import com.barberx.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for shop owner business onboarding.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final ShopOwnerProfileRepository shopOwnerProfileRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ShopResponse completeOnboarding(OnboardingRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("Completing onboarding for user: {}, shop: {}", currentUserId, request.getShopName());

        // Step 1: Find the owner profile
        ShopOwnerProfile profile = shopOwnerProfileRepository.findByUserIdAndDeletedFalse(currentUserId)
                .orElseThrow(() -> new CustomException("Shop Owner profile not found", HttpStatus.NOT_FOUND));

        if (profile.isProfileCompleted()) {
            throw new CustomException("Onboarding has already been completed for this account", HttpStatus.BAD_REQUEST);
        }

        User user = profile.getUser();

        // Step 2: Build the Shop entity
        Shop shop = Shop.builder()
                .owner(user)
                .ownerProfile(profile)
                .name(request.getShopName())
                .description(request.getShopDescription())
                .logoUrl(request.getLogoUrl())
                .coverImageUrl(request.getCoverImageUrl())
                .phone(request.getPhone())
                .email(user.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .businessType(request.getBusinessType())
                .employeeCount(request.getEmployeeCount())
                .verificationStatus(VerificationStatus.PENDING) // Always PENDING initially, reviewed by ADMIN
                .build();

        // Step 3: Populate Shop working hours
        List<ShopHours> shopHoursList = new ArrayList<>();
        if (request.getHours() != null) {
            for (OnboardingRequest.OnboardingHourEntry entry : request.getHours()) {
                ShopHours hours = ShopHours.builder()
                        .shop(shop)
                        .dayOfWeek(entry.getDayOfWeek().toUpperCase())
                        .openingTime(parseTime(entry.getOpeningTime()))
                        .closingTime(parseTime(entry.getClosingTime()))
                        .isHoliday(entry.isHoliday())
                        .build();
                shopHoursList.add(hours);
            }
        }
        shop.getShopHours().addAll(shopHoursList);

        // Save shop
        shop = shopRepository.save(shop);

        // Step 4: Update the Owner profile onboarding status
        profile.setOwnerName(request.getOwnerName());
        profile.setPhone(request.getPhone());
        profile.setProfileCompleted(true);
        shopOwnerProfileRepository.save(profile);

        // Step 5: Sync basic user fields
        user.setFullName(request.getOwnerName());
        user.setPhoneNumber(request.getPhone());
        userRepository.save(user);

        log.info("Onboarding completed successfully. Shop ID: {}", shop.getId());
        return ShopMapper.toResponse(shop, true);
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
