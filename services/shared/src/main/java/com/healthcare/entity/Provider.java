package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
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
@Table(name = DatabaseConstants.TABLE_PROVIDERS)
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
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Office phone must be a valid international format")
    @Column(name = DatabaseConstants.COL_OFFICE_PHONE)
    private String officePhone;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
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
        if (npiNumber == null || npiNumber.trim().isEmpty()) {
            throw new ValidationException("NPI number cannot be null or empty");
        }
        if (npiNumber.length() > 10) {
            throw new ValidationException("NPI number cannot exceed 10 characters");
        }
        if (!npiNumber.matches(ValidationPatterns.NPI)) {
            throw new ValidationException("NPI number must be exactly 10 digits");
        }
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
        if (officePhone != null && !officePhone.trim().isEmpty()) {
            if (officePhone.length() > 20) {
                throw new ValidationException("Office phone cannot exceed 20 characters");
            }
            if (!officePhone.matches(ValidationPatterns.PHONE)) {
                throw new ValidationException("Office phone must be a valid international format");
            }
        }
        this.officePhone = officePhone;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    // ==================== VALIDATION METHODS ====================

}
