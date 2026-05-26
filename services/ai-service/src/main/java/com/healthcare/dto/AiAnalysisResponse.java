package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.enums.AiTriggerType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AiAnalysisResponse(
        @JsonProperty("patient_id")        UUID patientId,
        @JsonProperty("last_encounter_id") UUID lastEncounterId,
        @JsonProperty("generated_at")      OffsetDateTime generatedAt,
        @JsonProperty("summary")           String summary,
        @JsonProperty("risk_flags")        List<RiskFlag> riskFlags,
        @JsonProperty("disclaimer")        String disclaimer,
        @JsonProperty("model_version")     String modelVersion,
        @JsonProperty("trigger_type")      AiTriggerType triggerType) {
}
