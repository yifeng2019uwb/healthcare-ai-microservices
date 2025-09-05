package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import com.fasterxml.jackson.databind.JsonNode;
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
@Table(name = DatabaseConstants.TABLE_PROVIDERS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_PROVIDERS_USER_ID_UNIQUE, columnList = DatabaseConstants.COL_USER_ID),
           @Index(name = DatabaseConstants.INDEX_PROVIDERS_NPI_NUMBER_UNIQUE, columnList = DatabaseConstants.COL_NPI_NUMBER),
           @Index(name = DatabaseConstants.INDEX_PROVIDERS_SPECIALTY, columnList = DatabaseConstants.COL_SPECIALTY)
       })
public class Provider extends BaseEntity {

    /**
     * Foreign key linking to user_profiles.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = DatabaseConstants.COL_USER_ID, nullable = false)
    private UUID userId;

    @Size(max = 50)
    @Column(name = DatabaseConstants.COL_LICENSE_NUMBERS)
    private String licenseNumbers;

    @NotBlank
    @Size(max = 10)
    @Pattern(regexp = ValidationPatterns.NPI, message = "NPI number must be exactly 10 digits")
    @Column(name = DatabaseConstants.COL_NPI_NUMBER, nullable = false, unique = true)
    private String npiNumber;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_SPECIALTY)
    private String specialty;

    @Column(name = DatabaseConstants.COL_QUALIFICATIONS, columnDefinition = "TEXT")
    private String qualifications;

    @Column(name = DatabaseConstants.COL_BIO, columnDefinition = "TEXT")
    private String bio;

    @Size(max = 20)
    @Column(name = DatabaseConstants.COL_OFFICE_PHONE)
    private String officePhone;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
    private JsonNode customData;

    // ==================== JPA RELATIONSHIPS ====================

    /**
     * Many-to-one relationship with User
     * Each provider belongs to exactly one user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_USER_ID, nullable = false, insertable = false, updatable = false)
    private User user;

    /**
     * One-to-many relationship with Appointments
     * A provider can have multiple appointments
     */
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Appointment> appointments = new java.util.ArrayList<>();

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
        this.licenseNumbers = ValidationUtils.validateAndNormalizeString(
            licenseNumbers,
            "License numbers",
            50
        );
    }

    public String getNpiNumber() {
        return npiNumber;
    }

    public void setNpiNumber(String npiNumber) {
        this.npiNumber = ValidationUtils.validateRequiredString(
            npiNumber,
            "NPI number",
            10,
            ValidationPatterns.NPI,
            "NPI number must be exactly 10 digits"
        );
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = ValidationUtils.validateAndNormalizeString(
            specialty,
            "Specialty",
            100
        );
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = ValidationUtils.validateAndNormalizeString(
            qualifications,
            "Qualifications",
            null,
            null,
            null
        );
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = ValidationUtils.validateAndNormalizeString(
            bio,
            "Bio",
            null,
            null,
            null
        );
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = ValidationUtils.validateAndNormalizeString(
            officePhone,
            "Office phone",
            20,
            ValidationPatterns.PHONE,
            "Office phone must be a valid international format"
        );
    }

    public JsonNode getCustomData() {
        return customData;
    }

    public void setCustomData(JsonNode customData) {
        this.customData = customData;
    }

    public User getUser() {
        return user;
    }

    public java.util.List<Appointment> getAppointments() {
        return appointments;
    }

}
