package com.healthcare.service;

import com.healthcare.entity.Patient;
import com.healthcare.entity.User;

import java.util.UUID;

/**
 * Service interface for Patient operations.
 * Defines the business logic contract for patient management.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
public interface PatientService {

    /**
     * Create a new patient account.
     * Creates both User and Patient entities in the database.
     *
     * @param user the user entity to create patient for
     * @return the created user entity with generated ID
     */
    User createPatient(User user);

    /**
     * Get user by ID.
     *
     * @param userId the user ID
     * @return the user entity
     */
    User getUserById(UUID userId);

    /**
     * Get patient by user ID.
     *
     * @param userId the user ID
     * @return the patient entity
     */
    Patient getPatientByUserId(UUID userId);

    /**
     * Get patient by patient number.
     *
     * @param patientNumber the patient number
     * @return the patient entity
     */
    Patient getPatientByNumber(String patientNumber);

    /**
     * Update patient information.
     *
     * @param patient the patient entity to update
     * @return the updated patient entity
     */
    Patient updatePatient(Patient patient);
}
