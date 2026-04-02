package com.healthcare.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Generic error response returned to clients.
 * Internal error codes and stack traces are never included.
 */
public record ErrorResponse(

        int status,
        String message,

        @JsonProperty("timestamp")
        Instant timestamp
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, Instant.now());
    }
}
