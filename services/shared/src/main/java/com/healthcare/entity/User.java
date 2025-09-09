package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * User entity representing system user profiles (patients and providers)
 * Maps to user_profiles table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_USER_PROFILES,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_USERS_EXTERNAL_AUTH_ID_UNIQUE, columnList = DatabaseConstants.COL_EXTERNAL_AUTH_ID),
           @Index(name = DatabaseConstants.INDEX_USERS_EMAIL_UNIQUE, columnList = DatabaseConstants.COL_EMAIL),
           @Index(name = DatabaseConstants.INDEX_USERS_PHONE, columnList = DatabaseConstants.COL_PHONE),
           @Index(name = DatabaseConstants.INDEX_USERS_NAME_DOB, columnList = DatabaseConstants.COL_LAST_NAME + "," + DatabaseConstants.COL_FIRST_NAME + "," + DatabaseConstants.COL_DATE_OF_BIRTH)
       })
public class User extends BaseEntity {


    @NotBlank
    @Size(max = 255)
    @Column(name = DatabaseConstants.COL_EXTERNAL_AUTH_ID, nullable = false, unique = true)
    private String externalAuthId; // External authentication provider ID - IMMUTABLE after creation

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "First name must contain only letters (including international characters), spaces, hyphens, and apostrophes")
    @Column(name = DatabaseConstants.COL_FIRST_NAME, nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = "Last name must contain only letters (including international characters), spaces, hyphens, and apostrophes")
    @Column(name = DatabaseConstants.COL_LAST_NAME, nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = DatabaseConstants.COL_EMAIL, nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Phone number must be a valid international format")
    @Column(name = DatabaseConstants.COL_PHONE, nullable = false)
    private String phone;

    @NotNull
    @Past(message = "Date of birth must be in the past")
    @Column(name = DatabaseConstants.COL_DATE_OF_BIRTH, nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_GENDER, nullable = false, columnDefinition = "VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN'))")
    private Gender gender;

    @Size(max = 255)
    @Column(name = DatabaseConstants.COL_STREET_ADDRESS)
    private String streetAddress;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_CITY)
    private String city;

    @Size(max = 50)
    @Column(name = DatabaseConstants.COL_STATE)
    private String state;

    @Size(max = 20)
    @Pattern(regexp = ValidationPatterns.POSTAL_CODE, message = "Postal code must be 3-20 characters with letters, numbers, spaces, or hyphens")
    @Column(name = DatabaseConstants.COL_POSTAL_CODE)
    private String postalCode;

    @Size(max = 50)
    @Column(name = DatabaseConstants.COL_COUNTRY)
    private String country;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ROLE, nullable = false, columnDefinition = "VARCHAR(20) CHECK (role IN ('PATIENT', 'PROVIDER'))")
    private UserRole role; // IMMUTABLE after creation

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_USER_STATUS, nullable = false, columnDefinition = "VARCHAR(20) CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))")
    private UserStatus status = UserStatus.ACTIVE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode customData;

    // ==================== JPA RELATIONSHIPS ====================

    /**
     * One-to-one relationship with Patient profile
     * Only populated when user role is PATIENT
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Patient patient;

    /**
     * One-to-one relationship with Provider profile
     * Only populated when user role is PROVIDER
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Provider provider;

    // ==================== CONSTRUCTORS ====================

    public User() {}

    public User(String externalAuthId, String firstName, String lastName, String email, String phone,
                LocalDate dateOfBirth, Gender gender, UserRole role) {
        this.externalAuthId = externalAuthId;
        this.role = role;
        this.status = UserStatus.ACTIVE;

        // Use setters to ensure consistent validation
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setPhone(phone);
        this.setDateOfBirth(dateOfBirth);
        this.setGender(gender);

    }

    // ==================== GETTERS ====================
    public String getExternalAuthId() {
        return externalAuthId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public JsonNode getCustomData() {
        return customData;
    }

    public Patient getPatient() {
        return patient;
    }

    public Provider getProvider() {
        return provider;
    }

    // ==================== SETTERS ====================
    public void setFirstName(String firstName) {
        this.firstName = ValidationUtils.validateRequiredString(
            firstName,
            "First name",
            100,
            ValidationPatterns.PERSON_NAME,
            "First name format is invalid"
        );
    }

    public void setLastName(String lastName) {
        this.lastName = ValidationUtils.validateRequiredString(
            lastName,
            "Last name",
            100,
            ValidationPatterns.PERSON_NAME,
            "Last name format is invalid"
        );
    }

    public void setEmail(String email) {
        this.email = ValidationUtils.validateRequiredString(
            email,
            "Email",
            255,
            ValidationPatterns.EMAIL,
            "Email format is invalid"
        );
    }

    public void setPhone(String phone) {
        this.phone = ValidationUtils.validateRequiredString(
            phone,
            "Phone",
            20,
            ValidationPatterns.PHONE,
            "Phone number format is invalid"
        );
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth cannot be null");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(Gender gender) {
        if (gender == null) {
            throw new ValidationException("Gender cannot be null");
        }
        this.gender = gender;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = ValidationUtils.validateAndNormalizeStringWithPattern(
            streetAddress,
            "Street address",
            ValidationPatterns.STREET_ADDRESS,
            "Street address format is invalid"
        );
    }

    public void setCity(String city) {
        this.city = ValidationUtils.validateAndNormalizeString(
            city,
            "City",
            100,
            ValidationPatterns.CITY_NAME,
            "City name format is invalid"
        );
    }

    public void setState(String state) {
        this.state = ValidationUtils.validateAndNormalizeString(
            state,
            "State",
            50,
            ValidationPatterns.STATE_NAME,
            "State name format is invalid"
        );
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = ValidationUtils.validateAndNormalizeString(
            postalCode,
            "Postal code",
            20,
            ValidationPatterns.POSTAL_CODE,
            "Postal code format is invalid"
        );
    }

    public void setCountry(String country) {
        this.country = ValidationUtils.validateAndNormalizeString(
            country,
            "Country",
            100,
            ValidationPatterns.COUNTRY_NAME,
            "Country name format is invalid"
        );
    }

    public void setStatus(UserStatus status) {
        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }
        this.status = status;
    }

    public void setCustomData(JsonNode customData) {
        this.customData = customData;
    }

    // ==================== HELPER METHODS ====================
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the user is at least 18 years old.
     *
     * @return true if user is 18 or older, false otherwise
     */
    public boolean isAdult() {
        return dateOfBirth.plusYears(18).isBefore(LocalDate.now());
    }

    /**
     * Validates that the user has a complete address.
     *
     * @return true if all address fields are present, false otherwise
     */
    public boolean hasCompleteAddress() {
        return streetAddress != null && city != null && state != null &&
               postalCode != null && country != null;
    }


    /**
     * Validates that the user's status allows for active operations.
     *
     * @return true if user is active, false otherwise
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

}
