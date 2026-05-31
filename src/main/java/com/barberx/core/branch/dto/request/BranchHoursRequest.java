package com.barberx.core.branch.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating branch hours.
 * Performs a full replace — all 7 days should be included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchHoursRequest {

    @NotEmpty(message = "Branch hours entries are required")
    @Valid
    private List<BranchHourEntry> hours;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchHourEntry {
        private String dayOfWeek;   // MONDAY, TUESDAY, ..., SUNDAY
        private String openingTime; // HH:mm format
        private String closingTime; // HH:mm format
        private boolean isHoliday;
    }
}
