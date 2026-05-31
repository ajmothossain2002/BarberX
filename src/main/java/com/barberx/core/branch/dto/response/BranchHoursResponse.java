package com.barberx.core.branch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single branch-hours entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchHoursResponse {

    private Long id;
    private String dayOfWeek;
    private String openingTime;
    private String closingTime;
    private boolean isHoliday;
}
