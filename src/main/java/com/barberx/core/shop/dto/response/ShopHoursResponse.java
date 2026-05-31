package com.barberx.core.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single shop-hours entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopHoursResponse {

    private Long id;
    private String dayOfWeek;
    private String openingTime;
    private String closingTime;
    private boolean isHoliday;
}
