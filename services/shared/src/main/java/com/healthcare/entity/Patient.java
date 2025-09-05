package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

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
    @Pattern(regexp = ValidationPatterns.INSURANCE_POLICY, message = "Insurance policy number must be 8-20 alphanumeric characters")
    @Column(name = DatabaseConstants.COL_INSURANCE_POLICY_NUMBER)
    private String insurancePolicyNumber;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_EMERGENCY_CONTACT_NAME)
    private String emergencyContactName;

    @Size(max = 20)
    @Pattern(regexp = ValidationPatterns.PHONE, message = "Emergency contact phone must be a valid international format")
    @Column(name = DatabaseConstants.COL_EMERGENCY_CONTACT_PHONE)
    private String emergencyContactPhone;

    @Size(max = 100)
    @Column(name = DatabaseConstants.COL_PRIMARY_CARE_PHYSICIAN)
    private String primaryCarePhysician;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
    private String customData;

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
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = currentMedications;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        if (insurancePolicyNumber != null && !insurancePolicyNumber.trim().isEmpty()) {
            if (insurancePolicyNumber.length() > 50) {
                throw new ValidationException("Insurance policy number cannot exceed 50 characters");
            }
            if (!insurancePolicyNumber.matches(ValidationPatterns.INSURANCE_POLICY)) {
                throw new ValidationException("Insurance policy number must be 8-20 alphanumeric characters");
            }
        }
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        if (emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty()) {
            if (emergencyContactPhone.length() > 20) {
                throw new ValidationException("Emergency contact phone cannot exceed 20 characters");
            }
            if (!emergencyContactPhone.matches(ValidationPatterns.PHONE)) {
                throw new ValidationException("Emergency contact phone must be a valid international format");
            }
        }
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getPrimaryCarePhysician() {
        return primaryCarePhysician;
    }

    public void setPrimaryCarePhysician(String primaryCarePhysician) {
        this.primaryCarePhysician = primaryCarePhysician;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
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
        return emergencyContactName != null && !emergencyContactName.trim().isEmpty() &&
               emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
    }

    /**
     * Checks if the patient has complete insurance information.
     * This is important for providers to know for billing and treatment purposes.
     *
     * @return true if both insurance provider and policy number are present, false otherwise
     */
    public boolean hasCompleteInsuranceInfo() {
        return insuranceProvider != null && !insuranceProvider.trim().isEmpty() &&
               insurancePolicyNumber != null && !insurancePolicyNumber.trim().isEmpty();
    }


}
