package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

/**
 * API Request for creating a new patient account
 *
 * This matches the design document specification for POST /api/patients
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@Setter
@NoArgsConstructor
public class CreatePatientAccountRequest {

    @NotBlank(message = "External user ID is required")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_EXTERNAL_USER_ID)
    private String externalUserId;

    @NotBlank(message = "First name is required")
    @Size(min = PatientServiceConstants.MIN_NAME_LENGTH, max = PatientServiceConstants.MAX_NAME_LENGTH,
          message = "First name must be between " + PatientServiceConstants.MIN_NAME_LENGTH + " and " + PatientServiceConstants.MAX_NAME_LENGTH + " characters")
    @Pattern(regexp = PatientServiceConstants.PATTERN_NAME_LETTERS_ONLY, message = "First name must contain only letters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_FIRST_NAME)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = PatientServiceConstants.MIN_NAME_LENGTH, max = PatientServiceConstants.MAX_NAME_LENGTH,
          message = "Last name must be between " + PatientServiceConstants.MIN_NAME_LENGTH + " and " + PatientServiceConstants.MAX_NAME_LENGTH + " characters")
    @Pattern(regexp = PatientServiceConstants.PATTERN_NAME_LETTERS_ONLY, message = "Last name must contain only letters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_LAST_NAME)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = PatientServiceConstants.MAX_EMAIL_LENGTH, message = "Email must not exceed " + PatientServiceConstants.MAX_EMAIL_LENGTH + " characters")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_EMAIL)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = PatientServiceConstants.PATTERN_PHONE_E164, message = "Phone must be in E.164 format (+1-555-0123)")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_PHONE)
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_DATE_OF_BIRTH)
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
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

    @Pattern(regexp = PatientServiceConstants.PATTERN_PHONE_E164, message = "Emergency contact phone must be in E.164 format (+1-555-0124)")
    @JsonProperty(PatientServiceConstants.JSON_FIELD_EMERGENCY_CONTACT_PHONE)
    private String emergencyContactPhone;

    // Lombok generates: @NoArgsConstructor, all getters and setters
}
