package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * API Request for updating patient info
 *
 * This matches the design document specification for PUT /api/patients/patient-info
 * Patients can update basic medical info like allergies, but not medical records.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdatePatientInfoRequest {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_MEDICAL_HISTORY)
    private Map<String, Object> medicalHistory;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_ALLERGIES)
    private Map<String, Object> allergies;

    @Size(max = PatientServiceConstants.MAX_INSURANCE_PROVIDER_LENGTH, message = "Insurance provider must not exceed " + PatientServiceConstants.MAX_INSURANCE_PROVIDER_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_PROVIDER)
    private String insuranceProvider;

    @Size(max = PatientServiceConstants.MAX_INSURANCE_POLICY_LENGTH, message = "Insurance policy number must not exceed " + PatientServiceConstants.MAX_INSURANCE_POLICY_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_POLICY_NUMBER)
    private String insurancePolicyNumber;

    @Size(max = PatientServiceConstants.MAX_PHYSICIAN_LENGTH, message = "Primary care physician must not exceed " + PatientServiceConstants.MAX_PHYSICIAN_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_PRIMARY_CARE_PHYSICIAN)
    private String primaryCarePhysician;

    // Lombok generates: @NoArgsConstructor, all getters and setters
}
