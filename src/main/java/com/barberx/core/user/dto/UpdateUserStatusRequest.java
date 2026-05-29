package com.barberx.core.user.dto;

import com.barberx.core.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a user's account status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a user's account status")
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New user status", example = "ACTIVE")
    private UserStatus status;
}
