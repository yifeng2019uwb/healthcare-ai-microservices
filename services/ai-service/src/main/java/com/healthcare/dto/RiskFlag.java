package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RiskFlag(
        @JsonProperty("flag")   String flag,
        @JsonProperty("reason") String reason) {
}
