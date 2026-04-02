package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Allergy;

import java.time.LocalDate;

/**
 * Response DTO for a single allergy in GET /api/patients/me/allergies.
 */
public record AllergyResponse(

        String code,
        String system,
        String description,

        @JsonProperty("allergy_type")
        String allergyType,

        String category,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("stop_date")
        LocalDate stopDate,

        boolean active,

        String reaction1,
        String description1,
        String severity1,

        String reaction2,
        String description2,
        String severity2,

        String notes
) {
    public static AllergyResponse from(Allergy a) {
        return new AllergyResponse(
                a.getCode(),
                a.getSystem(),
                a.getDescription(),
                a.getAllergyType(),
                a.getCategory(),
                a.getStartDate(),
                a.getStopDate(),
                a.isActive(),
                a.getReaction1(),
                a.getDescription1(),
                a.getSeverity1(),
                a.getReaction2(),
                a.getDescription2(),
                a.getSeverity2(),
                a.getNotes()
        );
    }
}
