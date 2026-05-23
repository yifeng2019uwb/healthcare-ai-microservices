package com.healthcare.dao;

import com.healthcare.entity.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO for Encounter entity.
 * Maps to encounters table — owned by appointment-service.
 */
@Repository
public interface EncounterDao extends JpaRepository<Encounter, UUID> {

    List<Encounter> findByPatientId(UUID patientId);

    List<Encounter> findByProviderId(UUID providerId);

    List<Encounter> findByProviderIdAndStartTimeBetween(
            UUID providerId, OffsetDateTime start, OffsetDateTime end);

    List<Encounter> findByPatientIdAndStartTimeBetween(
            UUID patientId, OffsetDateTime start, OffsetDateTime end);

    List<Encounter> findByProviderIdAndPatientId(UUID providerId, UUID patientId);

    /**
     * Check if a provider has at least one encounter with a patient.
     * Used for provider access authorization.
     */
    boolean existsByPatientIdAndProviderId(UUID patientId, UUID providerId);
}