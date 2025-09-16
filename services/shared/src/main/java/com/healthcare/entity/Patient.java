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
 * Patient entity representing patient-specific information
 * Maps to patient_profiles table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_PATIENTS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_PATIENTS_USER_ID_UNIQUE, columnList = DatabaseConstants.COL_USER_ID),
           @Index(name = DatabaseConstants.INDEX_PATIENTS_PATIENT_NUMBER_UNIQUE, columnList = DatabaseConstants.COL_PATIENT_NUMBER)
       })
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PATIENT_WITH_USER,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_USER)
        }
    ),
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PATIENT_WITH_APPOINTMENTS,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENTS)
        }
    ),
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_PATIENT_FULL_DETAILS,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_USER),
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENTS)
        }
    )
})
public class Patient extends BaseEntity {

    /**
     * Foreign key linking to user_profiles.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = DatabaseConstants.COL_USER_ID, nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = ValidationPatterns.PATIENT_NUMBER, message = "Patient number must be in format PAT-XXXXXXXX")
    @Column(name = DatabaseConstants.COL_PATIENT_NUMBER, nullable = false, unique = true)
    private String patientNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_MEDICAL_HISTORY, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode medicalHistory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_ALLERGIES, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode allergies;

    @Size(max = 2000)
    @Column(name = DatabaseConstants.COL_CURRENT_MEDICATIONS)
    private String currentMedications;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_INSURANCE_PROVIDER)
    private String insuranceProvider;

    @Size(max = 50)
    @Column(name = DatabaseConstants.COL_INSURANCE_POLICY_NUMBER)
    private String insurancePolicyNumber;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_EMERGENCY_CONTACT_NAME)
    private String emergencyContactName;

    @Size(max = 20)
    @Column(name = DatabaseConstants.COL_EMERGENCY_CONTACT_PHONE)
    private String emergencyContactPhone;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_PRIMARY_CARE_PHYSICIAN)
    private String primaryCarePhysician;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode customData;

    // ==================== JPA RELATIONSHIPS ====================

    /**
     * Many-to-one relationship with User
     * Each patient belongs to exactly one user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_USER_ID, nullable = false, insertable = false, updatable = false)
    private User user;

    /**
     * One-to-many relationship with Appointments
     * A patient can have multiple appointments
     *
     * Performance optimization:
     * - LAZY loading prevents unnecessary data fetching
     * - @BatchSize reduces N+1 query problems by batching related entity loads
     * - orphanRemoval ensures clean deletion of appointments
     */
    @OneToMany(mappedBy = DatabaseConstants.ATTR_PATIENT,
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
    private Patient() {}

    /**
     * Simple constructor for required fields only.
     * Use setters for optional fields.
     *
     * @param userId The ID of the user this patient belongs to
     * @param patientNumber The unique patient number (format: PAT-XXXXXXXX)
     */
    public Patient(UUID userId, String patientNumber) {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (patientNumber == null || patientNumber.trim().isEmpty()) {
            throw new ValidationException("Patient number is required");
        }

        this.userId = userId;
        this.patientNumber = patientNumber;
    }


    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validates that the patient object is in a valid state.
     * This should be called after object creation to ensure all required fields are set.
     *
     * @throws ValidationException if the patient is in an invalid state
     */
    public void validateState() {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (patientNumber == null || patientNumber.trim().isEmpty()) {
            throw new ValidationException("Patient number is required");
        }
        if (!patientNumber.matches(ValidationPatterns.PATIENT_NUMBER)) {
            throw new ValidationException("Patient number must be in format PAT-XXXXXXXX");
        }
    }


    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    // Note: userId is immutable after creation - use factory methods to create new patients

    public String getPatientNumber() {
        return patientNumber;
    }

    // Note: patientNumber is immutable after creation - use factory methods to create new patients

    public JsonNode getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(JsonNode medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public JsonNode getAllergies() {
        return allergies;
    }

    public void setAllergies(JsonNode allergies) {
        this.allergies = allergies;
    }

    public String getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = ValidationUtils.validateAndNormalizeString(
            currentMedications,
            "Current medications",
            2000
        );
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = ValidationUtils.validateAndNormalizeString(
            insuranceProvider,
            "Insurance provider",
            100
        );
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = ValidationUtils.validateAndNormalizeString(
            insurancePolicyNumber,
            "Insurance policy number",
            50,
            ValidationPatterns.INSURANCE_POLICY,
            "Insurance policy number must be 6-25 alphanumeric characters"
        );
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = ValidationUtils.validateAndNormalizeString(
            emergencyContactName,
            "Emergency contact name",
            100
        );
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = ValidationUtils.validateAndNormalizeString(
            emergencyContactPhone,
            "Emergency contact phone",
            20,
            ValidationPatterns.PHONE,
            "Emergency contact phone must be a valid international format"
        );
    }

    public String getPrimaryCarePhysician() {
        return primaryCarePhysician;
    }

    public void setPrimaryCarePhysician(String primaryCarePhysician) {
        this.primaryCarePhysician = ValidationUtils.validateAndNormalizeString(
            primaryCarePhysician,
            "Primary care physician",
            100
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

    // ==================== ENTITY METHODS ====================

    /**
     * Checks if the patient has complete emergency contact information.
     * This is important for healthcare safety and may be required in the future.
     *
     * @return true if both emergency contact name and phone are present, false otherwise
     */
    public boolean hasCompleteEmergencyContact() {
        return emergencyContactName != null && emergencyContactPhone != null;
    }

    /**
     * Checks if the patient has complete insurance information.
     * This is important for providers to know for billing and treatment purposes.
     *
     * @return true if both insurance provider and policy number are present, false otherwise
     */
    public boolean hasCompleteInsuranceInfo() {
        return insuranceProvider != null && insurancePolicyNumber != null;
    }


}
