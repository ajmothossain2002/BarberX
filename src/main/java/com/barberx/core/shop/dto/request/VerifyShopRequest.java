package com.barberx.core.shop.dto.request;

import com.barberx.core.shop.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for admin shop verification (approve/reject).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyShopRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    private String reason;
}
