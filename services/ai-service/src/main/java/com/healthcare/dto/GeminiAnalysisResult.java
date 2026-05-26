package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GeminiAnalysisResult(
        @JsonProperty("summary")    String summary,
        @JsonProperty("risk_flags") List<RiskFlag> riskFlags,
        @JsonProperty("disclaimer") String disclaimer) {
}
