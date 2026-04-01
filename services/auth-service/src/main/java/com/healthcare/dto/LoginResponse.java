package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for login and registration.
 * Returns access token + refresh token pair.
 */
public record LoginResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        int expiresIn
) {
    /**
     * Convenience factory — token type is always Bearer, access token expiry always 900s (15 min).
     */
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", 900);
    }
}