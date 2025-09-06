package com.healthcare.repository;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for Patient entity
 *
 * Provides basic CRUD operations and custom query methods
 * for Patient entity database operations.
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by user ID
     * Used by: GET /api/patients/profile, GET /api/patients/medical-history
     *
     * @param userId the user ID
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByUserId(UUID userId);

    /**
     * Find patient by patient number
     * Future use: Patient lookup by business key, reporting
     *
     * @param patientNumber the patient number
     * @return Optional containing the patient if found
     */
    Optional<Patient> findByPatientNumber(String patientNumber);

    // ==================== FUTURE METHODS ====================
    // TODO: Add these methods when needed

    /**
     * Find patients by status
     * Future use: Filtering, reporting
     */
    // List<Patient> findByStatus(PatientStatus status);

    /**
     * Find patients by insurance provider
     * Future use: Insurance reporting, filtering
     */
    // List<Patient> findByInsuranceProvider(String provider);

    /**
     * Find patients by primary care physician
     * Future use: Physician reporting, patient management
     */
    // List<Patient> findByPrimaryCarePhysician(String physician);

    /**
     * Check if patient number exists
     * Future use: Registration validation
     */
    // boolean existsByPatientNumber(String patientNumber);

    /**
     * Find patients by user ID list
     * Future use: Batch operations, reporting
     */
    // List<Patient> findByUserIdIn(List<UUID> userIds);
}
