package com.healthcare.service;

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
}
