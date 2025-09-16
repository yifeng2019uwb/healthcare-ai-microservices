package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

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
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PROVIDER_WITH_USER,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_USER)
        }
    ),
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PROVIDER_WITH_APPOINTMENTS,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENTS)
        }
    ),
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PROVIDER_FULL_DETAILS,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_USER),
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENTS)
        }
    )
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

    @Column(name = DatabaseConstants.COL_QUALIFICATIONS, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String qualifications;

    @Column(name = DatabaseConstants.COL_BIO, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String bio;

    @Size(max = 20)
    @Column(name = DatabaseConstants.COL_OFFICE_PHONE)
    private String officePhone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
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
     *
     * Performance optimization:
     * - LAZY loading prevents unnecessary data fetching
     * - @BatchSize reduces N+1 query problems by batching related entity loads
     * - orphanRemoval ensures clean deletion of appointments
     */
    @OneToMany(mappedBy = DatabaseConstants.ATTR_PROVIDER,
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @BatchSize(size = 20)
    private java.util.List<Appointment> appointments = new java.util.ArrayList<>();

    // ==================== CONSTRUCTORS ====================

    /**
     * Private constructor for JPA only.
     */
    @SuppressWarnings("unused")
    private Provider() {}

    /**
     * Simple constructor for required fields only.
     * Use setters for optional fields.
     *
     * @param userId The ID of the user this provider belongs to
     * @param npiNumber The National Provider Identifier (10 digits)
     */
    public Provider(UUID userId, String npiNumber) {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (npiNumber == null || npiNumber.trim().isEmpty()) {
            throw new ValidationException("NPI number is required");
        }

        this.userId = userId;
        this.npiNumber = npiNumber;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validates that the provider object is in a valid state.
     * This should be called after object creation to ensure all required fields are set.
     *
     * @throws ValidationException if the provider is in an invalid state
     */
    public void validateState() {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (npiNumber == null || npiNumber.trim().isEmpty()) {
            throw new ValidationException("NPI number is required");
        }
        if (!npiNumber.matches(ValidationPatterns.NPI)) {
            throw new ValidationException("NPI number must be exactly 10 digits");
        }
    }

    /**
     * Checks if this provider has complete professional credentials.
     *
     * @return true if provider has specialty, license numbers, and qualifications
     */
    public boolean hasCompleteCredentials() {
        return specialty != null && !specialty.trim().isEmpty() &&
               licenseNumbers != null && !licenseNumbers.trim().isEmpty() &&
               qualifications != null && !qualifications.trim().isEmpty();
    }

    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    // Note: userId is immutable after creation - use factory methods to create new providers

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

    // Note: npiNumber is immutable after creation - use factory methods to create new providers


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
