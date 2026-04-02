package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Condition;

import java.time.LocalDate;

/**
 * Response DTO for a single condition in GET /api/patients/me/conditions.
 */
public record ConditionResponse(

        String code,
        String system,
        String description,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("stop_date")
        LocalDate stopDate,

        boolean ongoing
) {
    public static ConditionResponse from(Condition c) {
        return new ConditionResponse(
                c.getCode(),
                c.getSystem(),
                c.getDescription(),
                c.getStartDate(),
                c.getStopDate(),
                c.isOngoing()
        );
    }
}
