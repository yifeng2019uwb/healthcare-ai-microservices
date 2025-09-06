package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * API Response for updating patient info
 * 
 * This matches the design document specification for PUT /api/patients/patient-info
 * 
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientInfoResponse {

    @JsonProperty("patientProfile")
    private PatientInfoResponse patientProfile;

    /**
     * Patient info response
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfoResponse {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_PATIENT_NUMBER)
        private String patientNumber;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_MEDICAL_HISTORY)
        private MedicalHistory medicalHistory;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_ALLERGIES)
        private Allergies allergies;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_PROVIDER)
        private String insuranceProvider;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_POLICY_NUMBER)
        private String insurancePolicyNumber;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_PRIMARY_CARE_PHYSICIAN)
        private String primaryCarePhysician;
        
        @JsonProperty(PatientServiceConstants.JSON_FIELD_UPDATED_AT)
        private String updatedAt;
    }
}
