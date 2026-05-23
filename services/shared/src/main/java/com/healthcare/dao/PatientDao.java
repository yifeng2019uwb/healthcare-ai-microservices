package com.healthcare.dao;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for Patient entity.
 * Maps to patients table — owned by patient-service.
 */
@Repository
public interface PatientDao extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by auth_id.
     * Used after login to fetch patient profile.
     */
    Optional<Patient> findByAuthId(UUID authId);

    /**
     * Check if auth_id is already linked.
     */
    boolean existsByAuthId(UUID authId);

    /**
     * Find patients by name + birthdate for registration matching.
     * Uses idx_patients_name index on (last_name, first_name, birthdate).
     * Returns a list — caller must enforce exactly one match.
     */
    List<Patient> findByFirstNameAndLastNameAndBirthdate(String firstName, String lastName, LocalDate birthdate);
}