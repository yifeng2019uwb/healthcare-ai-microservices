package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.healthcare.entity.Patient;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientSummaryResponse(UUID id, String name, String mrn) {

    public static PatientSummaryResponse from(Patient p) {
        if (p == null) return null;
        return new PatientSummaryResponse(
                p.getId(),
                p.getFirstName() + " " + p.getLastName(),
                p.getMrn());
    }
}
