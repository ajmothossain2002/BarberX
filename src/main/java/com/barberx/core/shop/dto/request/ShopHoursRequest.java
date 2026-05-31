package com.barberx.core.shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating shop hours.
 * Performs a full replace — all 7 days should be included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopHoursRequest {

    @NotEmpty(message = "Shop hours entries are required")
    @Valid
    private List<ShopHourEntry> hours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopHourEntry {
        private String dayOfWeek;   // MONDAY, TUESDAY, ..., SUNDAY
        private String openingTime; // HH:mm format
        private String closingTime; // HH:mm format
        private boolean isHoliday;
    }
}
