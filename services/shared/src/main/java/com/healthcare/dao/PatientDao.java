package com.healthcare.dao;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * DAO for Patient entity.
 * Maps to patients table — owned by patient-service.
 */
@Repository
public interface PatientDao extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by MRN.
     * Used for patient registration — MRN + first_name + last_name validation.
     */
    Optional<Patient> findByMrn(String mrn);

    /**
     * Find patient by auth_id.
     * Used after login to fetch patient profile.
     */
    Optional<Patient> findByAuthId(UUID authId);

    /**
     * Check if MRN exists.
     */
    boolean existsByMrn(String mrn);

    /**
     * Check if auth_id is already linked.
     */
    boolean existsByAuthId(UUID authId);
}