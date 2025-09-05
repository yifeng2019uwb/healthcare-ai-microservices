package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Patient entity representing patient-specific information
 * Maps to patient_profiles table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_PATIENTS)
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

    @Column(name = DatabaseConstants.COL_MEDICAL_HISTORY, columnDefinition = "JSONB")
    private String medicalHistory;

    @Column(name = DatabaseConstants.COL_ALLERGIES, columnDefinition = "JSONB")
    private String allergies;

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

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
    private JsonNode customData;

    // Constructors
    public Patient() {}

    // Constructor for service layer (with user ID only)
    public Patient(UUID userId, String patientNumber) {
        this.userId = userId;
        this.patientNumber = patientNumber;
    }

    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = ValidationUtils.validateAndNormalizeString(
            medicalHistory,
            "Medical history",
            (Integer) null,
            null,
            null
        );
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = ValidationUtils.validateAndNormalizeString(
            allergies,
            "Allergies",
            (Integer) null,
            null,
            null
        );
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
            "Insurance policy number must be 8-20 alphanumeric characters"
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
