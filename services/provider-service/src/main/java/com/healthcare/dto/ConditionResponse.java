package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Condition;

import java.time.LocalDate;

public record ConditionResponse(
        @JsonProperty("code")        String code,
        @JsonProperty("system")      String system,
        @JsonProperty("description") String description,
        @JsonProperty("start_date")  LocalDate startDate,
        @JsonProperty("stop_date")   LocalDate stopDate,
        @JsonProperty("status")      String status) {

    public static ConditionResponse from(Condition c) {
        return new ConditionResponse(
                c.getId().getCode(),
                c.getSystem(),
                c.getDescription(),
                c.getStartDate(),
                c.getStopDate(),
                c.getStopDate() == null ? "active" : "resolved");
    }
}
