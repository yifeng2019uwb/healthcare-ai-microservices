package com.healthcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Patient entity representing patient-specific information
 * Maps to patient_profiles table
 */
@Entity
@Table(name = "patient_profiles")
public class Patient extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^PAT-[0-9]{8}$", message = "Patient number must be in format PAT-XXXXXXXX")
    @Column(name = "patient_number", nullable = false, unique = true)
    private String patientNumber;

    @Column(name = "medical_history", columnDefinition = "JSONB")
    private String medicalHistory; // TODO: Consider using JSON converters for proper JSON handling

    @Column(name = "allergies", columnDefinition = "JSONB")
    private String allergies; // TODO: Consider using JSON converters for proper JSON handling

    @Size(max = 2000)
    @Column(name = "current_medications")
    private String currentMedications;

    @Size(max = 100)
    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9]{8,20}$", message = "Insurance policy number must be 8-20 alphanumeric characters")
    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Size(max = 100)
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Size(max = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Emergency contact phone must be a valid international format")
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Size(max = 100)
    @Column(name = "primary_care_physician")
    private String primaryCarePhysician;

    @Column(name = "custom_data", columnDefinition = "JSONB")
    private String customData;

    // Constructors
    public Patient() {}

    public Patient(User user, String patientNumber) {
        this.user = user;
        this.patientNumber = patientNumber;
    }

    // Getters and Setters

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
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

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the patient has complete emergency contact information.
     *
     * @return true if emergency contact is complete, false otherwise
     */
    public boolean hasCompleteEmergencyContact() {
        return emergencyContactName != null && !emergencyContactName.trim().isEmpty() &&
               emergencyContactPhone != null && !emergencyContactPhone.trim().isEmpty();
    }

    /**
     * Validates that the patient has insurance information.
     *
     * @return true if insurance information is present, false otherwise
     */
    public boolean hasInsuranceInfo() {
        return insuranceProvider != null && !insuranceProvider.trim().isEmpty() &&
               insurancePolicyNumber != null && !insurancePolicyNumber.trim().isEmpty();
    }

    /**
     * Validates that the patient number is in the correct format.
     *
     * @return true if patient number format is valid, false otherwise
     */
    public boolean hasValidPatientNumber() {
        if (patientNumber == null) {
            return false;
        }
        return patientNumber.matches("^PAT-[0-9]{8}$");
    }

    /**
     * Validates that the patient has medical history information.
     *
     * @return true if medical history is present, false otherwise
     */
    public boolean hasMedicalHistory() {
        return medicalHistory != null && !medicalHistory.trim().isEmpty();
    }

    /**
     * Validates that the patient has allergy information.
     *
     * @return true if allergy information is present, false otherwise
     */
    public boolean hasAllergyInfo() {
        return allergies != null && !allergies.trim().isEmpty();
    }

    /**
     * Validates that the patient has current medication information.
     *
     * @return true if current medications are present, false otherwise
     */
    public boolean hasCurrentMedications() {
        return currentMedications != null && !currentMedications.trim().isEmpty();
    }

    /**
     * Validates that the patient has a primary care physician.
     *
     * @return true if primary care physician is present, false otherwise
     */
    public boolean hasPrimaryCarePhysician() {
        return primaryCarePhysician != null && !primaryCarePhysician.trim().isEmpty();
    }

    /**
     * Validates that the patient is ready for appointment booking.
     *
     * @return true if patient is ready for appointments, false otherwise
     */
    public boolean isReadyForAppointments() {
        return user != null && user.isActive() && hasValidPatientNumber();
    }
}
