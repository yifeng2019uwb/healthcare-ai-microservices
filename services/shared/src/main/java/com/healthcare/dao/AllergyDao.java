package com.healthcare.dao;

import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * DAO for Allergy entity.
 * Maps to allergies table — composite PK (patient_id, encounter_id, code).
 * Owned by patient-service.
 */
@Repository
public interface AllergyDao extends JpaRepository<Allergy, AllergyId> {

    /**
     * Find all allergies for a patient.
     */
    List<Allergy> findByIdPatientId(UUID patientId);

    /**
     * Find all allergies recorded in a specific encounter.
     */
    List<Allergy> findByIdEncounterId(UUID encounterId);

    /**
     * Find active allergies for a patient (no stop date).
     */
    List<Allergy> findByIdPatientIdAndStopDateIsNull(UUID patientId);

    /**
     * Find allergies by category (environment, food, drug).
     */
    List<Allergy> findByIdPatientIdAndCategory(UUID patientId, String category);
}