package com.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Provider entity representing healthcare provider information
 * Maps to provider_profiles table
 */
@Entity
@Table(name = "provider_profiles")
public class Provider extends BaseEntity {

    /**
     * Foreign key linking to user_profiles.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Size(max = 50)
    @Column(name = "license_numbers")
    private String licenseNumbers;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = "^[0-9]{10}$", message = "NPI number must be exactly 10 digits")
    @Column(name = "npi_number", nullable = false, unique = true)
    private String npiNumber;

    @Size(max = 100)
    @Column(name = "specialty")
    private String specialty;

    @Column(name = "qualifications", columnDefinition = "TEXT")
    private String qualifications;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Size(max = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Office phone must be a valid international format")
    @Column(name = "office_phone")
    private String officePhone;

    @Column(name = "custom_data", columnDefinition = "JSONB")
    private String customData;

    // Constructors
    public Provider() {}

    // Constructor for service layer (with user ID only)
    public Provider(UUID userId, String npiNumber) {
        this.userId = userId;
        this.npiNumber = npiNumber;
    }

    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    public String getLicenseNumbers() {
        return licenseNumbers;
    }

    public void setLicenseNumbers(String licenseNumbers) {
        this.licenseNumbers = licenseNumbers;
    }

    public String getNpiNumber() {
        return npiNumber;
    }

    public void setNpiNumber(String npiNumber) {
        this.npiNumber = npiNumber;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the provider has a valid NPI number.
     *
     * @return true if NPI number is valid, false otherwise
     */
    public boolean hasValidNpiNumber() {
        if (npiNumber == null) {
            return false;
        }
        return npiNumber.matches("^[0-9]{10}$");
    }

    /**
     * Validates that the provider has license information.
     *
     * @return true if license numbers are present, false otherwise
     */
    public boolean hasLicenseInfo() {
        return licenseNumbers != null && !licenseNumbers.trim().isEmpty();
    }

    /**
     * Validates that the provider has specialty information.
     *
     * @return true if specialty is present, false otherwise
     */
    public boolean hasSpecialty() {
        return specialty != null && !specialty.trim().isEmpty();
    }

    /**
     * Validates that the provider has qualifications.
     *
     * @return true if qualifications are present, false otherwise
     */
    public boolean hasQualifications() {
        return qualifications != null && !qualifications.trim().isEmpty();
    }

    /**
     * Validates that the provider has office phone information.
     *
     * @return true if office phone is present, false otherwise
     */
    public boolean hasOfficePhone() {
        return officePhone != null && !officePhone.trim().isEmpty();
    }

    /**
     * Validates that the provider is ready to accept appointments.
     *
     * @return true if provider is ready for appointments, false otherwise
     */
    public boolean isReadyForAppointments() {
        return user != null && user.isActive() && hasValidNpiNumber() && hasSpecialty();
    }

    /**
     * Validates that the provider has complete professional information.
     *
     * @return true if provider has complete info, false otherwise
     */
    public boolean hasCompleteProfessionalInfo() {
        return hasValidNpiNumber() && hasSpecialty() && hasQualifications();
    }
}
