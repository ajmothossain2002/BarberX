package com.barberx.core.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for exposing role information in management APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information for management APIs")
public class RoleDto {

    @Schema(description = "Role ID", example = "1")
    private Long id;

    @Schema(description = "Role name", example = "ADMIN")
    private String name;

    @Schema(description = "Role description", example = "System Administrator")
    private String description;

    @Schema(description = "Permission names assigned to this role")
    private List<String> permissions;
}
