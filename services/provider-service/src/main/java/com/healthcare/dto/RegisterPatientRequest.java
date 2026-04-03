package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterPatientRequest(
        @NotBlank @Size(max = 100)
        @JsonProperty("first_name")         String firstName,

        @Size(max = 100)
        @JsonProperty("middle_name")        String middleName,

        @NotBlank @Size(max = 100)
        @JsonProperty("last_name")          String lastName,

        @JsonProperty("birthdate")          LocalDate birthdate,

        @Size(max = 1)
        @JsonProperty("gender")             String gender,

        @Size(max = 50)
        @JsonProperty("race")               String race,

        @Size(max = 50)
        @JsonProperty("ethnicity")          String ethnicity,

        @Size(max = 255)
        @JsonProperty("address")            String address,

        @Size(max = 100)
        @JsonProperty("city")               String city,

        @Size(max = 2)
        @JsonProperty("state")              String state,

        @Size(max = 10)
        @JsonProperty("zip")                String zip,

        @Size(max = 30)
        @JsonProperty("phone")              String phone,

        @Size(max = 255)
        @JsonProperty("emergency_contact")  String emergencyContact,

        @Size(max = 10)
        @JsonProperty("blood_type")         String bloodType,

        @JsonProperty("notes")              String notes) {
}
