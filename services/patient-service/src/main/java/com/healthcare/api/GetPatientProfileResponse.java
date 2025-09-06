package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * API Response for getting patient profile
 *
 * This matches the design document specification for GET /api/patients/profile
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetPatientProfileResponse {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_USER_PROFILE)
    private UserProfile userProfile;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_PATIENT_PROFILE)
    private PatientProfile patientProfile;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_EXTERNAL_USER_ID)
        private String externalUserId;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_FIRST_NAME)
        private String firstName;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_LAST_NAME)
        private String lastName;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_EMAIL)
        private String email;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_PHONE)
        private String phone;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_DATE_OF_BIRTH)
        private String dateOfBirth;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_GENDER)
        private String gender;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_STREET_ADDRESS)
        private String streetAddress;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CITY)
        private String city;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_STATE)
        private String state;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_POSTAL_CODE)
        private String postalCode;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_COUNTRY)
        private String country;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_ROLE)
        private String role;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_STATUS)
        private String status;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CREATED_AT)
        private String createdAt;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_UPDATED_AT)
        private String updatedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientProfile {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_PATIENT_NUMBER)
        private String patientNumber;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_MEDICAL_HISTORY)
        private Map<String, Object> medicalHistory;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_ALLERGIES)
        private Map<String, Object> allergies;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_PROVIDER)
        private String insuranceProvider;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_INSURANCE_POLICY_NUMBER)
        private String insurancePolicyNumber;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_PRIMARY_CARE_PHYSICIAN)
        private String primaryCarePhysician;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_NAME)
        private String emergencyContactName;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_PHONE)
        private String emergencyContactPhone;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CREATED_AT)
        private String createdAt;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_UPDATED_AT)
        private String updatedAt;
    }

    // Lombok generates: @NoArgsConstructor, @AllArgsConstructor, and all getters
}
