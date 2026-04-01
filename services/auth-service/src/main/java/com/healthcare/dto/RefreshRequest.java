package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token refresh.
 */
public record RefreshRequest(

        @NotBlank(message = "Refresh token is required")
        @JsonProperty("refresh_token")
        String refreshToken
) {}