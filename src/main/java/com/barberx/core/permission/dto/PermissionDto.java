package com.barberx.core.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for exposing permission information in management APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission information for management APIs")
public class PermissionDto {

    @Schema(description = "Permission ID", example = "1")
    private Long id;

    @Schema(description = "Permission name", example = "USER_CREATE")
    private String name;

    @Schema(description = "Permission description", example = "Allows creating new users")
    private String description;

    @Schema(description = "Module this permission belongs to", example = "USER")
    private String module;
}
