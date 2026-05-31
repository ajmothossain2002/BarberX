package com.barberx.core.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for branch data — never exposes the raw entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BranchResponse {

    private Long id;
    private Long shopId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isActive;

    // Hours (optional — included when fetching single branch)
    private List<BranchHoursResponse> branchHours;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
