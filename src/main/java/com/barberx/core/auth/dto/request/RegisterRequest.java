package com.barberx.core.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request payload")
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "User's password (min 8 characters)", example = "SecurePass123!")
    private String password;

    @Schema(description = "Role name to assign (defaults to USER)", example = "USER")
    private String roleName;
}
