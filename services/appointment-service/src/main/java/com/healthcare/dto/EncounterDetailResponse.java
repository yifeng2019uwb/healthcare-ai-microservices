package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Encounter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EncounterDetailResponse(
        UUID id,

        @JsonProperty("start_time")
        OffsetDateTime startTime,

        @JsonProperty("stop_time")
        OffsetDateTime stopTime,

        @JsonProperty("encounter_class")
        String encounterClass,

        String code,
        String description,

        @JsonProperty("reason_code")
        String reasonCode,

        @JsonProperty("reason_desc")
        String reasonDesc,

        @JsonProperty("base_cost")
        BigDecimal baseCost,

        @JsonProperty("total_cost")
        BigDecimal totalCost,

        @JsonProperty("payer_coverage")
        BigDecimal payerCoverage,

        ProviderSummaryResponse provider,
        OrganizationSummaryResponse organization) {

    public static EncounterDetailResponse from(Encounter e) {
        return new EncounterDetailResponse(
                e.getId(),
                e.getStartTime(),
                e.getStopTime(),
                e.getEncounterClass(),
                e.getCode(),
                e.getDescription(),
                e.getReasonCode(),
                e.getReasonDesc(),
                e.getBaseCost(),
                e.getTotalCost(),
                e.getPayerCoverage(),
                ProviderSummaryResponse.detail(e.getProvider()),
                OrganizationSummaryResponse.detail(e.getOrganization()));
    }
}
