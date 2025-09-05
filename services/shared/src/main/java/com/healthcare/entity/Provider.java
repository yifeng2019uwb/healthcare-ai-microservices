package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Column(name = DatabaseConstants.COL_OFFICE_PHONE)
    private String officePhone;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
    private JsonNode customData;

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
        if (licenseNumbers != null) {
            licenseNumbers = licenseNumbers.trim();
            if (licenseNumbers.isBlank()) {
                licenseNumbers = null;  // Normalize to NULL
            } else if (licenseNumbers.length() > 50) {
                throw new ValidationException("License numbers cannot exceed 50 characters");
            }
        }
        this.licenseNumbers = licenseNumbers;
    }

    public String getNpiNumber() {
        return npiNumber;
    }

    public void setNpiNumber(String npiNumber) {
        if (npiNumber == null || npiNumber.trim().isEmpty()) {
            throw new ValidationException("NPI number cannot be null or empty");
        }
        npiNumber = npiNumber.trim();
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
        if (specialty != null) {
            specialty = specialty.trim();
            if (specialty.isBlank()) {
                specialty = null;  // Normalize to NULL
            } else if (specialty.length() > 100) {
                throw new ValidationException("Specialty cannot exceed 100 characters");
            }
        }
        this.specialty = specialty;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        if (qualifications != null) {
            qualifications = qualifications.trim();
            if (qualifications.isBlank()) {
                qualifications = null;  // Normalize to NULL
            }
        }
        this.qualifications = qualifications;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        if (bio != null) {
            bio = bio.trim();
            if (bio.isBlank()) {
                bio = null;  // Normalize to NULL
            }
        }
        this.bio = bio;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        if (officePhone != null) {
            officePhone = officePhone.trim();
            if (officePhone.isBlank()) {
                officePhone = null;  // Normalize to NULL
            } else if (officePhone.length() > 20) {
                throw new ValidationException("Office phone cannot exceed 20 characters");
            } else if (!officePhone.matches(ValidationPatterns.PHONE)) {
                throw new ValidationException("Office phone must be a valid international format");
            }
        }
        this.officePhone = officePhone;
    }

    public JsonNode getCustomData() {
        return customData;
    }

    public void setCustomData(JsonNode customData) {
        this.customData = customData;
    }

    public void setCustomData(String customDataJson) {
        if (customDataJson == null) {
            this.customData = null;
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.customData = mapper.readTree(customDataJson);
        } catch (Exception e) {
            throw new ValidationException("Invalid JSON format for custom data: " + e.getMessage());
        }
    }

    // ==================== VALIDATION METHODS ====================

}
