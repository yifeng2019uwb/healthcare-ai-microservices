package com.healthcare.dao;

import com.healthcare.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for Provider entity.
 * Maps to providers table — owned by provider-service.
 */
@Repository
public interface ProviderDao extends JpaRepository<Provider, UUID> {

    /**
     * Find provider by auth_id.
     * Used after login to fetch provider profile.
     */
    Optional<Provider> findByAuthId(UUID authId);

    /**
     * Find all providers in an organization.
     */
    List<Provider> findByOrganizationId(UUID organizationId);

    /**
     * Find active providers by speciality.
     */
    List<Provider> findBySpecialityAndIsActive(String speciality, Boolean isActive);

    /**
     * Check if auth_id is already linked.
     */
    boolean existsByAuthId(UUID authId);

    /**
     * Find providers by name + organization for registration matching.
     * Uses idx_providers_name index on (name, organization_id).
     * Synthea data only — NPI and license are not available from Synthea import.
     * Returns a list — caller must enforce exactly one match.
     */
    List<Provider> findByNameAndOrganizationId(String name, UUID organizationId);

    /**
     * Find providers by full name for auth registration lookup.
     * Uses idx_providers_name index prefix scan on name column.
     * Returns a list — caller must enforce exactly one match.
     */
    List<Provider> findByName(String name);
}