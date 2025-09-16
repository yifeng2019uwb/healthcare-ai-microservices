package com.healthcare.dao;

import com.healthcare.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for MedicalRecord entity
 * Handles database operations for medical records
 */
@Repository
public interface MedicalRecordDao extends JpaRepository<MedicalRecord, UUID> {

    /**
     * Find medical records by appointment ID
     * Used to get all medical records for a specific appointment
     *
     * @param appointmentId The appointment ID
     * @return List of medical records for the appointment
     */
    List<MedicalRecord> findByAppointmentId(UUID appointmentId);

    /**
     * Find medical records by patient ID
     * Used to get all medical records for a specific patient
     *
     * @param patientId The patient ID
     * @return List of medical records for the patient
     */
    List<MedicalRecord> findByPatientId(UUID patientId);

    /**
     * Create a new medical record
     * Saves the medical record entity to the database
     *
     * @param medicalRecord The medical record entity to create
     * @return The created medical record entity
     */
    default MedicalRecord create(MedicalRecord medicalRecord) {
        return save(medicalRecord);
    }

    /**
     * Update an existing medical record
     * Updates the medical record entity in the database
     *
     * @param medicalRecord The medical record entity to update
     * @return The updated medical record entity
     */
    default MedicalRecord update(MedicalRecord medicalRecord) {
        return save(medicalRecord);
    }
}
