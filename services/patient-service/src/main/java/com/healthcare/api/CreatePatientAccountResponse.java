package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * API Response for creating a new patient account
 *
 * This matches the design document specification for POST /api/patients
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientAccountResponse {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_SUCCESS)
    private boolean success;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_MESSAGE)
    private String message;

    // Lombok generates: @NoArgsConstructor, @AllArgsConstructor, and all getters

    // Manual constructor for now
    public CreatePatientAccountResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
