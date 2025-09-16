package com.healthcare.dao;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for Patient entity
 * Handles database operations for patient profiles
 */
@Repository
public interface PatientDao extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by user ID
     * Used to get patient profile from user ID (1:1 relationship)
     *
     * @param userId The user ID
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByUserId(UUID userId);

    /**
     * Find patient by patient number
     * Used for patient lookup by unique patient number
     *
     * @param patientNumber The patient number (format: PAT-XXXXXXXX)
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByPatientNumber(String patientNumber);

    /**
     * Create a new patient
     * Saves the patient entity to the database
     *
     * @param patient The patient entity to create
     * @return The created patient entity
     */
    default Patient create(Patient patient) {
        return save(patient);
    }

    /**
     * Update an existing patient
     * Updates the patient entity in the database
     *
     * @param patient The patient entity to update
     * @return The updated patient entity
     */
    default Patient update(Patient patient) {
        return save(patient);
    }
}
