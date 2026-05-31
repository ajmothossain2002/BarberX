package com.barberx.core.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with JWT token and user details")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "User's main role name", example = "OWNER")
    private String role;

    @Schema(description = "Whether the user's business onboarding profile is completed", example = "true")
    private Boolean profileCompleted;

    @Schema(description = "The ID of the owner's configured shop, if any", example = "1")
    private Long shopId;

    @Schema(description = "The authoritative next route the frontend should navigate to", example = "/owner/dashboard")
    private String nextRoute;

    @Schema(description = "Authenticated user information")
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Authenticated user profile information")
    public static class UserInfo {

        @Schema(description = "User ID", example = "1")
        private Long id;

        @Schema(description = "User's full name", example = "John Doe")
        private String fullName;

        @Schema(description = "User's email address", example = "john.doe@example.com")
        private String email;

        @Schema(description = "User's phone number", example = "+1234567890")
        private String phoneNumber;

        @Schema(description = "Assigned roles", example = "[\"USER\"]")
        private List<String> roles;

        @Schema(description = "Whether the user's business onboarding profile is completed", example = "true")
        private Boolean profileCompleted;
    }
}
