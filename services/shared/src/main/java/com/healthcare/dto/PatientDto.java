package com.healthcare.dto;

import com.healthcare.entity.Patient;
import com.healthcare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.Optional;

/**
 * DTO for Patient entity CRUD operations
 *
 * Handles database operations for Patient entity with focus on
 * Patient Service API requirements.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Component
public class PatientDto {

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Create a new patient
     * Used by: POST /api/patients (Create Patient Account)
     * Note: May not be needed if database auto-creates patient profile
     *
     * @param patient the patient entity to create
     * @return the saved patient entity
     */
    public Patient createPatient(Patient patient) {
        // Database generates UUID via gen_random_uuid() DEFAULT
        // Entity ID will be null until after save() operation
        return patientRepository.save(patient);
    }

    /**
     * Update patient entity
     * Used by: PUT /api/patients/patient-info (Update Patient Info)
     *
     * @param patient the patient entity to update
     * @return the updated patient entity
     */
    public Patient updatePatient(Patient patient) {
        // For updates, patient must exist and have an ID
        if (patient.getId() == null) {
            throw new IllegalArgumentException("Patient ID is required for update");
        }

        // Check if patient exists
        if (!patientRepository.existsById(patient.getId())) {
            throw new IllegalArgumentException("Patient with ID " + patient.getId() + " does not exist");
        }

        return patientRepository.save(patient);
    }

    /**
     * Get patient by user ID
     * Used by: GET /api/patients/profile (Get Patient Profile)
     * Used by: GET /api/patients/medical-history (Get Medical History)
     *
     * @param userId the user ID
     * @return the patient entity or null if not found
     */
    public Patient getPatientByUserId(UUID userId) {
        Optional<Patient> patient = patientRepository.findByUserId(userId);
        return patient.orElse(null);
    }

    /**
     * Get patient by ID
     * Used by: GET /api/patients/profile (Get Patient Profile)
     * Used by: GET /api/patients/medical-history (Get Medical History)
     *
     * @param patientId the patient ID
     * @return the patient entity or null if not found
     */
    public Patient getPatientById(UUID patientId) {
        return patientRepository.findById(patientId).orElse(null);
    }


    // ==================== FUTURE METHODS ====================
    // TODO: Implement these methods when needed

    /**
     * Get patient by patient number
     * Future use: Patient lookup by business key, reporting
     */
    // public Patient getPatientByNumber(String patientNumber) {
    //     return patientRepository.findByPatientNumber(patientNumber).orElse(null);
    // }

    /**
     * Get all patients
     * Future use: Admin operations, reporting
     */
    // public List<Patient> getAllPatients() {
    //     return patientRepository.findAll();
    // }

    /**
     * Get patients by status
     * Future use: Filtering, reporting
     */
    // public List<Patient> getPatientsByStatus(PatientStatus status) {
    //     return patientRepository.findByStatus(status);
    // }

    /**
     * Get patients by insurance provider
     * Future use: Insurance reporting, filtering
     */
    // public List<Patient> getPatientsByInsuranceProvider(String provider) {
    //     return patientRepository.findByInsuranceProvider(provider);
    // }

    /**
     * Get patients by primary care physician
     * Future use: Physician reporting, patient management
     */
    // public List<Patient> getPatientsByPrimaryCarePhysician(String physician) {
    //     return patientRepository.findByPrimaryCarePhysician(physician);
    // }

    /**
     * Delete patient by ID
     * Future use: Patient management, data cleanup
     */
    // public void deletePatient(UUID patientId) {
    //     // TODO: Soft delete or hard delete based on business rules
    //     patientRepository.deleteById(patientId);
    // }
}
