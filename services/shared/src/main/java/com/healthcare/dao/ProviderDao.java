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
     * Find provider by provider_code.
     * Used for provider registration validation.
     */
    Optional<Provider> findByProviderCode(String providerCode);

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
     * Check if provider_code exists.
     */
    boolean existsByProviderCode(String providerCode);

    /**
     * Check if auth_id is already linked.
     */
    boolean existsByAuthId(UUID authId);
}