package com.barberx.core.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a user's role assignment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a user's role")
public class UpdateUserRoleRequest {

    @NotBlank(message = "Role name is required")
    @Schema(description = "The role name to assign", example = "BARBER")
    private String roleName;
}
