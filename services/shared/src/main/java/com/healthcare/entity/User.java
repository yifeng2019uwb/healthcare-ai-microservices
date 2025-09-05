package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * User entity representing system user profiles (patients and providers)
 * Maps to user_profiles table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_USER_PROFILES)
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
    @Column(name = DatabaseConstants.COL_GENDER, nullable = false)
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
    @Column(name = DatabaseConstants.COL_ROLE, nullable = false)
    private UserRole role; // IMMUTABLE after creation

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_USER_STATUS, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
    private JsonNode customData;

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

    // ==================== SETTERS ====================
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First name cannot be null or empty");
        }
        firstName = firstName.trim();
        if (firstName.length() > 100) {
            throw new ValidationException("First name cannot exceed 100 characters");
        }
        if (!firstName.matches(ValidationPatterns.PERSON_NAME)) {
            throw new ValidationException("First name format is invalid");
        }
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last name cannot be null or empty");
        }
        lastName = lastName.trim();
        if (lastName.length() > 100) {
            throw new ValidationException("Last name cannot exceed 100 characters");
        }
        if (!lastName.matches(ValidationPatterns.PERSON_NAME)) {
            throw new ValidationException("Last name format is invalid");
        }
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        email = email.trim();
        if (email.length() > 255) {
            throw new ValidationException("Email cannot exceed 255 characters");
        }
        if (!email.matches(ValidationPatterns.EMAIL)) {
            throw new ValidationException("Email format is invalid");
        }
        this.email = email;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone cannot be null or empty");
        }
        phone = phone.trim();
        if (phone.length() > 20) {
            throw new ValidationException("Phone cannot exceed 20 characters");
        }
        if (!phone.matches(ValidationPatterns.PHONE)) {
            throw new ValidationException("Phone number format is invalid");
        }
        this.phone = phone;
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
        if (streetAddress != null) {
            streetAddress = streetAddress.trim();
            if (streetAddress.isBlank()) {
                streetAddress = null;  // Normalize to NULL
            } else if (!streetAddress.matches(ValidationPatterns.STREET_ADDRESS)) {
                throw new ValidationException("Street address format is invalid");
            }
        }
        this.streetAddress = streetAddress;
    }


    public void setCity(String city) {
        if (city != null) {
            city = city.trim();
            if (city.isBlank()) {
                city = null;  // Normalize to NULL
            } else if (city.length() > 100) {
                throw new ValidationException("City cannot exceed 100 characters");
            } else if (!city.matches(ValidationPatterns.PERSON_NAME)) {
                throw new ValidationException("City name format is invalid");
            }
        }
        this.city = city;
    }

    public void setState(String state) {
        if (state != null) {
            state = state.trim();
            if (state.isBlank()) {
                state = null;  // Normalize to NULL
            } else if (state.length() > 50) {
                throw new ValidationException("State cannot exceed 50 characters");
            } else if (!state.matches(ValidationPatterns.PERSON_NAME)) {
                throw new ValidationException("State name format is invalid");
            }
        }
        this.state = state;
    }

    public void setPostalCode(String postalCode) {
        if (postalCode != null) {
            postalCode = postalCode.trim();
            if (postalCode.isBlank()) {
                postalCode = null;  // Normalize to NULL
            } else if (postalCode.length() > 20) {
                throw new ValidationException("Postal code cannot exceed 20 characters");
            } else if (!postalCode.matches(ValidationPatterns.POSTAL_CODE)) {
                throw new ValidationException("Postal code format is invalid");
            }
        }
        this.postalCode = postalCode;
    }

    public void setCountry(String country) {
        if (country != null) {
            country = country.trim();
            if (country.isBlank()) {
                country = null;  // Normalize to NULL
            } else if (country.length() > 100) {
                throw new ValidationException("Country cannot exceed 100 characters");
            } else if (!country.matches(ValidationPatterns.PERSON_NAME)) {
                throw new ValidationException("Country name format is invalid");
            }
        }
        this.country = country;
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

    /**
     * Checks if the user is marked as deleted.
     *
     * @return true if user is deleted, false otherwise
     */
    public boolean isDeleted() {
        return status == UserStatus.DELETED;
    }
}
