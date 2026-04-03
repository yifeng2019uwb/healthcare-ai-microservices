package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Patient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientProfileResponse(
        @JsonProperty("id")                 UUID id,
        @JsonProperty("mrn")                String mrn,
        @JsonProperty("first_name")         String firstName,
        @JsonProperty("middle_name")        String middleName,
        @JsonProperty("last_name")          String lastName,
        @JsonProperty("birthdate")          LocalDate birthdate,
        @JsonProperty("gender")             String gender,
        @JsonProperty("race")               String race,
        @JsonProperty("ethnicity")          String ethnicity,
        @JsonProperty("address")            String address,
        @JsonProperty("city")               String city,
        @JsonProperty("state")              String state,
        @JsonProperty("zip")                String zip,
        @JsonProperty("phone")              String phone,
        @JsonProperty("emergency_contact")  String emergencyContact,
        @JsonProperty("blood_type")         String bloodType,
        @JsonProperty("notes")              String notes,
        @JsonProperty("created_at")         OffsetDateTime createdAt,
        @JsonProperty("updated_at")         OffsetDateTime updatedAt) {

    public static PatientProfileResponse from(Patient p) {
        return new PatientProfileResponse(
                p.getId(),
                p.getMrn(),
                p.getFirstName(),
                p.getMiddleName(),
                p.getLastName(),
                p.getBirthdate(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getRace(),
                p.getEthnicity(),
                p.getAddress(),
                p.getCity(),
                p.getState(),
                p.getZip(),
                p.getPhone(),
                p.getEmergencyContact(),
                p.getBloodType(),
                p.getNotes(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }
}
