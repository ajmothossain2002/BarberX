package com.barberx.core.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO capturing all steps of the shop owner business onboarding wizard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    // Step 1: Owner Details
    @NotBlank(message = "Owner name is required")
    @Size(max = 150, message = "Owner name must not exceed 150 characters")
    private String ownerName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    // Step 2: Shop Details
    @NotBlank(message = "Shop name is required")
    @Size(max = 255, message = "Shop name must not exceed 255 characters")
    private String shopName;

    private String shopDescription;
    private String logoUrl;
    private String coverImageUrl;

    // Step 3: Location
    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    // Step 4: Business Details
    @NotBlank(message = "Business type is required")
    private String businessType; // Barber Shop, Salon, Unisex Salon, Spa

    @NotNull(message = "Employee count is required")
    private Integer employeeCount;

    // Step 5: Working Schedule
    @NotEmpty(message = "Working hours are required")
    @Valid
    private List<OnboardingHourEntry> hours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingHourEntry {
        private String dayOfWeek;   // MONDAY, TUESDAY, ..., SUNDAY
        private String openingTime; // HH:mm format
        private String closingTime; // HH:mm format
        private boolean isHoliday;
    }
}
