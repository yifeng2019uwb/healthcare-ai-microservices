package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.AuthConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for patient registration.
 *
 * Validates MRN + first_name + last_name against the patients table.
 * Bean Validation handles API boundary rules — domain rules enforced in AuthService.
 */
public record RegisterPatientRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(regexp = AuthConstants.USERNAME_PATTERN,
                message = "Username can only contain letters, numbers and underscores")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        // BCrypt silently truncates at 72 bytes — enforce the limit explicitly
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
        @Pattern(regexp = AuthConstants.PASSWORD_PATTERN,
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character (@$!%*?&^#)")
        String password,

        @NotBlank(message = "MRN is required")
        String mrn,

        @JsonProperty("first_name")
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @JsonProperty("last_name")
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName
) {}