package com.healthcare.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("status") int status,
        @JsonProperty("message") String message) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message);
    }
}
