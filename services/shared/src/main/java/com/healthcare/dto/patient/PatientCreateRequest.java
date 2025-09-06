package com.healthcare.dto.patient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserStatus;
import com.healthcare.constants.ValidationPatterns;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new patient profile
 *
 * <p>This DTO contains all the necessary information to create a new patient
 * profile, including personal information, contact details, medical history,
 * and insurance information.</p>
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public class PatientCreateRequest {

    @NotBlank(message = "External auth ID is required")
    @Size(max = 255, message = "External auth ID must not exceed 255 characters")
    @JsonProperty("external_auth_id")
    private String externalAuthId;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "First name contains invalid characters")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "Last name contains invalid characters")
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must be a valid phone number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @JsonProperty("phone")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @JsonProperty("gender")
    private Gender gender;

    @Size(max = 255, message = "Street address must not exceed 255 characters")
    @JsonProperty("street_address")
    private String streetAddress;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Pattern(regexp = ValidationPatterns.CITY_NAME, message = "City contains invalid characters")
    @JsonProperty("city")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.STATE_NAME, message = "State contains invalid characters")
    @JsonProperty("state")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Pattern(regexp = ValidationPatterns.POSTAL_CODE, message = "Postal code contains invalid characters")
    @JsonProperty("postal_code")
    private String postalCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    @Pattern(regexp = ValidationPatterns.COUNTRY_NAME, message = "Country contains invalid characters")
    @JsonProperty("country")
    private String country;

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "Emergency contact name contains invalid characters")
    @JsonProperty("emergency_contact_name")
    private String emergencyContactName;

    @Pattern(regexp = ValidationPatterns.PHONE, message = "Emergency contact phone must be a valid phone number")
    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    @JsonProperty("emergency_contact_phone")
    private String emergencyContactPhone;

    @JsonProperty("emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    @JsonProperty("insurance_provider")
    private String insuranceProvider;

    @Pattern(regexp = ValidationPatterns.INSURANCE_POLICY, message = "Insurance policy number contains invalid characters")
    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    @JsonProperty("insurance_policy_number")
    private String insurancePolicyNumber;

    @JsonProperty("insurance_type")
    private String insuranceType;

    @JsonProperty("medical_conditions")
    private List<String> medicalConditions;

    @JsonProperty("allergies")
    private List<String> allergies;

    @JsonProperty("medications")
    private List<String> medications;

    @JsonProperty("status")
    private UserStatus status = UserStatus.ACTIVE;

    // Constructors
    public PatientCreateRequest() {}

    public PatientCreateRequest(String externalAuthId, String firstName, String lastName,
                              String email, String phone, LocalDate dateOfBirth, Gender gender) {
        this.externalAuthId = externalAuthId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // Getters and Setters
    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public List<String> getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(List<String> medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getMedications() {
        return medications;
    }

    public void setMedications(List<String> medications) {
        this.medications = medications;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
