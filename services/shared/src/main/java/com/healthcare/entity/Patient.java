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
        if (medicalHistory != null) {
            medicalHistory = medicalHistory.trim();
            if (medicalHistory.isBlank()) {
                medicalHistory = null;  // Normalize to NULL
            }
        }
        this.medicalHistory = medicalHistory;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        if (allergies != null) {
            allergies = allergies.trim();
            if (allergies.isBlank()) {
                allergies = null;  // Normalize to NULL
            }
        }
        this.allergies = allergies;
    }

    public String getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(String currentMedications) {
        if (currentMedications != null) {
            currentMedications = currentMedications.trim();
            if (currentMedications.isBlank()) {
                currentMedications = null;  // Normalize to NULL
            } else if (currentMedications.length() > 2000) {
                throw new ValidationException("Current medications cannot exceed 2000 characters");
            }
        }
        this.currentMedications = currentMedications;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        if (insuranceProvider != null) {
            insuranceProvider = insuranceProvider.trim();
            if (insuranceProvider.isBlank()) {
                insuranceProvider = null;  // Normalize to NULL
            } else if (insuranceProvider.length() > 100) {
                throw new ValidationException("Insurance provider cannot exceed 100 characters");
            }
        }
        this.insuranceProvider = insuranceProvider;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        if (insurancePolicyNumber != null) {
            insurancePolicyNumber = insurancePolicyNumber.trim();
            if (insurancePolicyNumber.isBlank()) {
                insurancePolicyNumber = null;  // Normalize to NULL
            } else if (insurancePolicyNumber.length() > 50) {
                throw new ValidationException("Insurance policy number cannot exceed 50 characters");
            } else if (!insurancePolicyNumber.matches(ValidationPatterns.INSURANCE_POLICY)) {
                throw new ValidationException("Insurance policy number must be 8-20 alphanumeric characters");
            }
        }
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        if (emergencyContactName != null) {
            emergencyContactName = emergencyContactName.trim();
            if (emergencyContactName.isBlank()) {
                emergencyContactName = null;  // Normalize to NULL
            } else if (emergencyContactName.length() > 100) {
                throw new ValidationException("Emergency contact name cannot exceed 100 characters");
            }
        }
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        if (emergencyContactPhone != null) {
            emergencyContactPhone = emergencyContactPhone.trim();
            if (emergencyContactPhone.isBlank()) {
                emergencyContactPhone = null;  // Normalize to NULL
            } else if (emergencyContactPhone.length() > 20) {
                throw new ValidationException("Emergency contact phone cannot exceed 20 characters");
            } else if (!emergencyContactPhone.matches(ValidationPatterns.PHONE)) {
                throw new ValidationException("Emergency contact phone must be a valid international format");
            }
        }
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getPrimaryCarePhysician() {
        return primaryCarePhysician;
    }

    public void setPrimaryCarePhysician(String primaryCarePhysician) {
        if (primaryCarePhysician != null) {
            primaryCarePhysician = primaryCarePhysician.trim();
            if (primaryCarePhysician.isBlank()) {
                primaryCarePhysician = null;  // Normalize to NULL
            } else if (primaryCarePhysician.length() > 100) {
                throw new ValidationException("Primary care physician cannot exceed 100 characters");
            }
        }
        this.primaryCarePhysician = primaryCarePhysician;
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
