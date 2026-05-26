package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddAllergyRequest(
        @NotBlank
        @JsonProperty("code")          String code,
        @JsonProperty("description")   String description,
        @NotNull
        @JsonProperty("start_date")    LocalDate startDate,
        @JsonProperty("stop_date")     LocalDate stopDate,
        @JsonProperty("allergy_type")  String allergyType,
        @JsonProperty("category")      String category,
        @JsonProperty("reaction1")     String reaction1,
        @JsonProperty("description1")  String description1,
        @JsonProperty("severity1")     String severity1,
        @JsonProperty("reaction2")     String reaction2,
        @JsonProperty("description2")  String description2,
        @JsonProperty("severity2")     String severity2) {
}
