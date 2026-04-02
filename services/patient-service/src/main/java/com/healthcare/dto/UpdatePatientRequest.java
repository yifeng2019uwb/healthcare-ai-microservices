package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for PUT /api/patients/me.
 * Only fields the patient is allowed to update themselves.
 * All fields are optional — null means no change.
 */
public record UpdatePatientRequest(

        @Size(max = 20)
        @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Phone must be a valid international format")
        String phone,

        @JsonProperty("emergency_contact")
        @Size(max = 255)
        String emergencyContact,

        @Size(max = 255)
        String address,

        @Size(max = 100)
        String city,

        @Size(max = 2)
        String state,

        @Size(max = 10)
        String zip,

        @Size(max = 10000)
        String notes
) {}
