package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for logout.
 *
 * Access token is read from Authorization header by AuthController.
 * Refresh token is passed in the request body so both can be blacklisted.
 */
public record LogoutRequest(

        @NotBlank(message = "Refresh token is required")
        @JsonProperty("refresh_token")
        String refreshToken
) {}