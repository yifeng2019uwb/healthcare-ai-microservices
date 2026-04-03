package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Allergy;

import java.time.LocalDate;

public record AllergyResponse(
        @JsonProperty("code")         String code,
        @JsonProperty("system")       String system,
        @JsonProperty("description")  String description,
        @JsonProperty("allergy_type") String allergyType,
        @JsonProperty("category")     String category,
        @JsonProperty("severity1")    String severity1,
        @JsonProperty("start_date")   LocalDate startDate,
        @JsonProperty("stop_date")    LocalDate stopDate) {

    public static AllergyResponse from(Allergy a) {
        return new AllergyResponse(
                a.getId().getCode(),
                a.getSystem(),
                a.getDescription(),
                a.getAllergyType(),
                a.getCategory(),
                a.getSeverity1(),
                a.getStartDate(),
                a.getStopDate());
    }
}
