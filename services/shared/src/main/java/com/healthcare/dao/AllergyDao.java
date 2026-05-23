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

    List<Allergy> findByIdPatientId(UUID patientId);
}