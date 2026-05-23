package com.healthcare.dto;

import com.healthcare.constants.AuthConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for provider registration.
 *
 * Bean Validation handles API boundary rules — domain rules enforced in AuthService.
 */
public record RegisterProviderRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(regexp = AuthConstants.USERNAME_PATTERN,
                 message = "Username can only contain letters, numbers and underscores")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        // Min 8 — NIST baseline. Max 32 — practical limit. BCrypt hard limit is 72 bytes.
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
        @Pattern(regexp = AuthConstants.PASSWORD_PATTERN,
                 message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character (@$!%*?&^#)")
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name
) {}