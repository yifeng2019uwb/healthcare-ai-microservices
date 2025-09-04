package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
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

/**
 * User entity representing system user profiles (patients and providers)
 * Maps to user_profiles table
 */
@Entity
@Table(name = "user_profiles")
public class User extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "external_auth_id", nullable = false, unique = true)
    private String externalAuthId; // External authentication provider ID - rename to authId when internal auth is added

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be a valid international format")
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Size(max = 255)
    @Column(name = "street_address")
    private String streetAddress;

    @Size(max = 100)
    @Column(name = "city")
    private String city;

    @Size(max = 50)
    @Column(name = "state")
    private String state;

    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9\\s-]{3,20}$", message = "Postal code must be 3-20 characters with letters, numbers, spaces, or hyphens")
    @Column(name = "postal_code")
    private String postalCode;

    @Size(max = 50)
    @Column(name = "country")
    private String country;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "custom_data", columnDefinition = "JSONB")
    private String customData;

    // ==================== CONSTRUCTORS ====================

    public User() {}

    public User(String externalAuthId, String firstName, String lastName, String email, String phone,
                LocalDate dateOfBirth, Gender gender, UserRole role) {
        this.externalAuthId = externalAuthId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.role = role;
        this.status = UserStatus.ACTIVE;
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

    public String getCustomData() {
        return customData;
    }

    // ==================== SETTERS ====================

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setCustomData(String customData) {
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
        if (dateOfBirth == null) {
            return false;
        }
        return dateOfBirth.plusYears(18).isBefore(LocalDate.now()) ||
               dateOfBirth.plusYears(18).isEqual(LocalDate.now());
    }

    /**
     * Validates that the user has a complete address.
     *
     * @return true if all address fields are present, false otherwise
     */
    public boolean hasCompleteAddress() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               postalCode != null && !postalCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }

    /**
     * Validates that the user's email domain is valid for healthcare.
     *
     * @return true if email domain is valid, false otherwise
     */
    public boolean hasValidHealthcareEmail() {
        if (email == null) {
            return false;
        }
        String[] validDomains = {"gmail.com", "outlook.com", "yahoo.com", "healthcare.gov", "hospital.org"};
        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        for (String validDomain : validDomains) {
            if (domain.equals(validDomain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that the user's phone number is in a valid format.
     *
     * @return true if phone number is valid, false otherwise
     */
    public boolean hasValidPhoneNumber() {
        if (phone == null) {
            return false;
        }
        // Remove all non-digit characters except +
        String cleanPhone = phone.replaceAll("[^\\d+]", "");
        return cleanPhone.length() >= 10 && cleanPhone.length() <= 15;
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
     * Validates that the user can be updated.
     *
     * @return true if user can be updated, false otherwise
     */
    public boolean canBeUpdated() {
        return isActive() && !isDeleted();
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
