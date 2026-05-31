package com.barberx.core.shop.dto.response;

import com.barberx.core.shop.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for shop data — never exposes the raw entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShopResponse {

    private Long id;

    // Owner info
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    // Basic info
    private String name;
    private String description;
    private String logoUrl;
    private String coverImageUrl;
    private String phone;
    private String email;
    private String website;

    // Location
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Verification
    private VerificationStatus verificationStatus;

    private String businessType;
    private Integer employeeCount;
    private BigDecimal averageRating;
    private BigDecimal startingPrice;

    // Hours (optional — included when fetching single shop)
    private List<ShopHoursResponse> shopHours;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
