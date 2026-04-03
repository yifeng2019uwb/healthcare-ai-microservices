package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterPatientResponse(
        @JsonProperty("id")         UUID id,
        @JsonProperty("mrn")        String mrn,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name")  String lastName,
        @JsonProperty("birthdate")  LocalDate birthdate,
        @JsonProperty("gender")     String gender,
        @JsonProperty("created_at") OffsetDateTime createdAt) {

    public static RegisterPatientResponse from(Patient p) {
        return new RegisterPatientResponse(
                p.getId(),
                p.getMrn(),
                p.getFirstName(),
                p.getLastName(),
                p.getBirthdate(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getCreatedAt());
    }
}
