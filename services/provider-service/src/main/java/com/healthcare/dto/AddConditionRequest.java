package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddConditionRequest(
        @NotBlank
        @JsonProperty("code")        String code,
        @JsonProperty("description") String description,
        @NotNull
        @JsonProperty("start_date")  LocalDate startDate,
        @JsonProperty("stop_date")   LocalDate stopDate) {
}
