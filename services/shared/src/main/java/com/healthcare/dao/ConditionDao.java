package com.healthcare.dao;

import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * DAO for Condition entity.
 * Maps to conditions table — composite PK (patient_id, encounter_id, code).
 * Owned by patient-service.
 */
@Repository
public interface ConditionDao extends JpaRepository<Condition, ConditionId> {

    List<Condition> findByIdPatientId(UUID patientId);

    List<Condition> findByIdCode(String code);
}