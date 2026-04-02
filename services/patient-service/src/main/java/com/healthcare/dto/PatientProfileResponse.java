package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Patient;
import com.healthcare.enums.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for GET /api/patients/me.
 * Maps Patient entity fields safe to expose to the patient themselves.
 * Excludes: SSN, drivers, passport, deathdate, lat/lon, financial fields.
 */
public record PatientProfileResponse(

        UUID id,
        String mrn,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("middle_name")
        String middleName,

        @JsonProperty("last_name")
        String lastName,

        String prefix,
        String suffix,
        String birthdate,
        Gender gender,
        String race,
        String ethnicity,
        String phone,

        @JsonProperty("emergency_contact")
        String emergencyContact,

        String address,
        String city,
        String state,
        String zip,

        @JsonProperty("blood_type")
        String bloodType,

        String notes,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt
) {
    public static PatientProfileResponse from(Patient p) {
        LocalDate bd = p.getBirthdate();
        return new PatientProfileResponse(
                p.getId(),
                p.getMrn(),
                p.getFirstName(),
                p.getMiddleName(),
                p.getLastName(),
                p.getPrefix(),
                p.getSuffix(),
                bd != null ? bd.toString() : null,
                p.getGender(),
                p.getRace(),
                p.getEthnicity(),
                p.getPhone(),
                p.getEmergencyContact(),
                p.getAddress(),
                p.getCity(),
                p.getState(),
                p.getZip(),
                p.getBloodType(),
                p.getNotes(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
