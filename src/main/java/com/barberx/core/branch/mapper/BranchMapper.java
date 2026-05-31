package com.barberx.core.branch.mapper;

import com.barberx.core.branch.dto.request.CreateBranchRequest;
import com.barberx.core.branch.dto.request.UpdateBranchRequest;
import com.barberx.core.branch.dto.response.BranchHoursResponse;
import com.barberx.core.branch.dto.response.BranchResponse;
import com.barberx.core.branch.entity.Branch;
import com.barberx.core.branch.entity.BranchHours;
import com.barberx.core.shop.entity.Shop;

import java.util.List;

/**
 * Stateless mapper utility for converting Branch entities to DTOs and vice versa.
 * Follows the manual-mapper pattern used in the codebase.
 */
public final class BranchMapper {

    private BranchMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a CreateBranchRequest to a new Branch entity.
     */
    public static Branch toEntity(CreateBranchRequest request, Shop shop) {
        return Branch.builder()
                .shop(shop)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
    }

    /**
     * Maps a Branch entity to a BranchResponse DTO (without hours).
     */
    public static BranchResponse toResponse(Branch branch) {
        return toResponse(branch, false);
    }

    /**
     * Maps a Branch entity to a BranchResponse DTO, optionally including hours.
     */
    public static BranchResponse toResponse(Branch branch, boolean includeHours) {
        BranchResponse.BranchResponseBuilder builder = BranchResponse.builder()
                .id(branch.getId())
                .shopId(branch.getShop().getId())
                .name(branch.getName())
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .address(branch.getAddress())
                .city(branch.getCity())
                .state(branch.getState())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .isActive(branch.isActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt());

        if (includeHours && branch.getBranchHours() != null) {
            List<BranchHoursResponse> hours = branch.getBranchHours().stream()
                    .filter(h -> !h.isDeleted())
                    .map(BranchMapper::toHoursResponse)
                    .toList();
            builder.branchHours(hours);
        }

        return builder.build();
    }

    /**
     * Maps a BranchHours entity to a BranchHoursResponse DTO.
     */
    public static BranchHoursResponse toHoursResponse(BranchHours hours) {
        return BranchHoursResponse.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .openingTime(hours.getOpeningTime() != null ? hours.getOpeningTime().toString() : null)
                .closingTime(hours.getClosingTime() != null ? hours.getClosingTime().toString() : null)
                .isHoliday(hours.isHoliday())
                .build();
    }

    /**
     * Updates an existing Branch entity with non-null fields from UpdateBranchRequest.
     */
    public static void updateEntity(Branch branch, UpdateBranchRequest request) {
        if (request.getName() != null) branch.setName(request.getName());
        if (request.getPhone() != null) branch.setPhone(request.getPhone());
        if (request.getEmail() != null) branch.setEmail(request.getEmail());
        if (request.getAddress() != null) branch.setAddress(request.getAddress());
        if (request.getCity() != null) branch.setCity(request.getCity());
        if (request.getState() != null) branch.setState(request.getState());
        if (request.getLatitude() != null) branch.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) branch.setLongitude(request.getLongitude());
    }
}
