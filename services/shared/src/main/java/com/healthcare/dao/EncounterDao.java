package com.healthcare.dao;

import com.healthcare.entity.Encounter;
import com.healthcare.enums.EncounterStatus;
import com.healthcare.enums.EncounterType;
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

    List<Encounter> findByOrganizationId(UUID organizationId);

    List<Encounter> findByStatus(EncounterStatus status);

    List<Encounter> findByPatientIdAndStatus(UUID patientId, EncounterStatus status);

    List<Encounter> findByProviderIdAndStatus(UUID providerId, EncounterStatus status);

    List<Encounter> findByEncounterType(EncounterType encounterType);

    List<Encounter> findByStartTimeBetween(OffsetDateTime start, OffsetDateTime end);

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