package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Encounter;
import com.healthcare.enums.EncounterStatus;
import com.healthcare.enums.EncounterType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for a single encounter in GET /api/patients/me/encounters.
 */
public record EncounterResponse(

        UUID id,

        @JsonProperty("provider_id")
        UUID providerId,

        @JsonProperty("organization_id")
        UUID organizationId,

        @JsonProperty("start_time")
        OffsetDateTime startTime,

        @JsonProperty("stop_time")
        OffsetDateTime stopTime,

        @JsonProperty("encounter_class")
        String encounterClass,

        EncounterStatus status,

        @JsonProperty("encounter_type")
        EncounterType encounterType,

        String code,
        String description,

        @JsonProperty("base_cost")
        BigDecimal baseCost,

        @JsonProperty("total_cost")
        BigDecimal totalCost,

        @JsonProperty("reason_code")
        String reasonCode,

        @JsonProperty("reason_desc")
        String reasonDesc
) {
    public static EncounterResponse from(Encounter e) {
        return new EncounterResponse(
                e.getId(),
                e.getProviderId(),
                e.getOrganizationId(),
                e.getStartTime(),
                e.getStopTime(),
                e.getEncounterClass(),
                e.getStatus(),
                e.getEncounterType(),
                e.getCode(),
                e.getDescription(),
                e.getBaseCost(),
                e.getTotalCost(),
                e.getReasonCode(),
                e.getReasonDesc()
        );
    }
}
