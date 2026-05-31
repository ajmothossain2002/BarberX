package com.barberx.core.shop.mapper;

import com.barberx.core.shop.dto.request.CreateShopRequest;
import com.barberx.core.shop.dto.request.UpdateShopRequest;
import com.barberx.core.shop.dto.response.ShopHoursResponse;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.entity.ShopHours;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.entity.ShopOwnerProfile;

import java.util.Collections;
import java.util.List;

/**
 * Stateless mapper utility for converting Shop entities to DTOs and vice versa.
 * Follows the same manual-mapper pattern used by UserMapper.
 */
public final class ShopMapper {

    private ShopMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a CreateShopRequest to a new Shop entity.
     */
    public static Shop toEntity(CreateShopRequest request, User owner) {
        return toEntity(request, owner, null);
    }

    /**
     * Maps a CreateShopRequest to a new Shop entity with ShopOwnerProfile.
     */
    public static Shop toEntity(CreateShopRequest request, User owner, ShopOwnerProfile ownerProfile) {
        return Shop.builder()
                .owner(owner)
                .ownerProfile(ownerProfile)
                .name(request.getName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
    }

    /**
     * Maps a Shop entity to a ShopResponse DTO (without hours).
     */
    public static ShopResponse toResponse(Shop shop) {
        return toResponse(shop, false);
    }

    /**
     * Maps a Shop entity to a ShopResponse DTO, optionally including hours.
     */
    public static ShopResponse toResponse(Shop shop, boolean includeHours) {
        ShopResponse.ShopResponseBuilder builder = ShopResponse.builder()
                .id(shop.getId())
                .ownerId(shop.getOwner().getId())
                .ownerName(shop.getOwner().getFullName())
                .ownerEmail(shop.getOwner().getEmail())
                .name(shop.getName())
                .description(shop.getDescription())
                .logoUrl(shop.getLogoUrl())
                .coverImageUrl(shop.getCoverImageUrl())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .website(shop.getWebsite())
                .address(shop.getAddress())
                .city(shop.getCity())
                .state(shop.getState())
                .country(shop.getCountry())
                .postalCode(shop.getPostalCode())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .verificationStatus(shop.getVerificationStatus())
                .businessType(shop.getBusinessType())
                .employeeCount(shop.getEmployeeCount())
                .averageRating(null)
                .startingPrice(null)
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt());

        if (includeHours && shop.getShopHours() != null) {
            List<ShopHoursResponse> hours = shop.getShopHours().stream()
                    .filter(h -> !h.isDeleted())
                    .map(ShopMapper::toHoursResponse)
                    .toList();
            builder.shopHours(hours);
        }

        return builder.build();
    }

    /**
     * Maps a ShopHours entity to a ShopHoursResponse DTO.
     */
    public static ShopHoursResponse toHoursResponse(ShopHours hours) {
        return ShopHoursResponse.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openingTime(hours.getOpeningTime() != null ? hours.getOpeningTime().toString() : null)
                .closingTime(hours.getClosingTime() != null ? hours.getClosingTime().toString() : null)
                .isHoliday(hours.isHoliday())
                .build();
    }

    /**
     * Updates an existing Shop entity with non-null fields from UpdateShopRequest.
     */
    public static void updateEntity(Shop shop, UpdateShopRequest request) {
        if (request.getName() != null) shop.setName(request.getName());
        if (request.getDescription() != null) shop.setDescription(request.getDescription());
        if (request.getPhone() != null) shop.setPhone(request.getPhone());
        if (request.getEmail() != null) shop.setEmail(request.getEmail());
        if (request.getWebsite() != null) shop.setWebsite(request.getWebsite());
        if (request.getAddress() != null) shop.setAddress(request.getAddress());
        if (request.getCity() != null) shop.setCity(request.getCity());
        if (request.getState() != null) shop.setState(request.getState());
        if (request.getCountry() != null) shop.setCountry(request.getCountry());
        if (request.getPostalCode() != null) shop.setPostalCode(request.getPostalCode());
        if (request.getLatitude() != null) shop.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) shop.setLongitude(request.getLongitude());
    }
}
