package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * API Request for updating personal profile
 *
 * This matches the design document specification for PUT /api/patients/profile
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdatePersonalProfileRequest {

    @Size(min = PatientServiceConstants.MIN_NAME_LENGTH, max = PatientServiceConstants.MAX_NAME_LENGTH,
          message = "First name must be between " + PatientServiceConstants.MIN_NAME_LENGTH + " and " + PatientServiceConstants.MAX_NAME_LENGTH + " characters")
    @Pattern(regexp = PatientServiceConstants.PATTERN_NAME_LETTERS_ONLY, message = "First name must contain only letters, spaces, hyphens, and apostrophes")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_FIRST_NAME)
    private String firstName;

    @Size(min = PatientServiceConstants.MIN_NAME_LENGTH, max = PatientServiceConstants.MAX_NAME_LENGTH,
          message = "Last name must be between " + PatientServiceConstants.MIN_NAME_LENGTH + " and " + PatientServiceConstants.MAX_NAME_LENGTH + " characters")
    @Pattern(regexp = PatientServiceConstants.PATTERN_NAME_LETTERS_ONLY, message = "Last name must contain only letters, spaces, hyphens, and apostrophes")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_LAST_NAME)
    private String lastName;

    @Pattern(regexp = PatientServiceConstants.PATTERN_PHONE_E164, message = "Phone must be in international format (+1234567890)")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_PHONE)
    private String phone;

    @Pattern(regexp = PatientServiceConstants.PATTERN_GENDER_VALUES, message = "Gender must be MALE, FEMALE, OTHER, or UNKNOWN")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_GENDER)
    private String gender;

    @Size(max = PatientServiceConstants.MAX_ADDRESS_LENGTH, message = "Street address must not exceed " + PatientServiceConstants.MAX_ADDRESS_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_STREET_ADDRESS)
    private String streetAddress;

    @Size(max = PatientServiceConstants.MAX_CITY_LENGTH, message = "City must not exceed " + PatientServiceConstants.MAX_CITY_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_CITY)
    private String city;

    @Size(max = PatientServiceConstants.MAX_STATE_LENGTH, message = "State must not exceed " + PatientServiceConstants.MAX_STATE_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_STATE)
    private String state;

    @Size(max = PatientServiceConstants.MAX_POSTAL_CODE_LENGTH, message = "Postal code must not exceed " + PatientServiceConstants.MAX_POSTAL_CODE_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_POSTAL_CODE)
    private String postalCode;

    @Size(max = PatientServiceConstants.MAX_COUNTRY_LENGTH, message = "Country must not exceed " + PatientServiceConstants.MAX_COUNTRY_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_COUNTRY)
    private String country;

    @Size(max = PatientServiceConstants.MAX_EMERGENCY_CONTACT_NAME_LENGTH, message = "Emergency contact name must not exceed " + PatientServiceConstants.MAX_EMERGENCY_CONTACT_NAME_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_NAME)
    private String emergencyContactName;

    @Pattern(regexp = PatientServiceConstants.PATTERN_PHONE_E164, message = "Emergency contact phone must be in international format (+1234567890)")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_PHONE)
    private String emergencyContactPhone;

    // Lombok generates: @NoArgsConstructor, all getters and setters
}
