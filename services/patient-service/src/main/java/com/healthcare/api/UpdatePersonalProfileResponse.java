package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * API Response for updating personal profile
 *
 * This matches the design document specification for PUT /api/patients/profile
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalProfileResponse {

    @JsonProperty("userProfile")
    private UserProfileResponse userProfile;

    @JsonProperty("patientProfile")
    private PatientProfileResponse patientProfile;

    /**
     * User profile information
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileResponse {
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

    /**
     * Patient profile information
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientProfileResponse {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_PATIENT_NUMBER)
        private String patientNumber;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_NAME)
        private String emergencyContactName;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_PHONE)
        private String emergencyContactPhone;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CREATED_AT)
        private String createdAt;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_UPDATED_AT)
        private String updatedAt;
    }
}
