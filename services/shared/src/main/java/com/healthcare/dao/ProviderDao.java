package com.healthcare.dao;

import com.healthcare.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for Provider entity
 * Handles database operations for provider profiles
 */
@Repository
public interface ProviderDao extends JpaRepository<Provider, UUID> {

    /**
     * Find provider by user ID
     * Used to get provider profile from user ID (1:1 relationship)
     *
     * @param userId The user ID
     * @return Optional containing the provider if found
     */
    Optional<Provider> findByUserId(UUID userId);

    /**
     * Find provider by NPI number
     * Used for provider lookup by unique NPI number
     *
     * @param npiNumber The NPI number (10 digits)
     * @return Optional containing the provider if found
     */
    Optional<Provider> findByNpiNumber(String npiNumber);

    /**
     * Find providers by specialty
     * Used for provider search by medical specialty
     *
     * @param specialty The medical specialty
     * @return List of providers with the specified specialty
     */
    List<Provider> findBySpecialty(String specialty);

    /**
     * Create a new provider
     * Saves the provider entity to the database
     *
     * @param provider The provider entity to create
     * @return The created provider entity
     */
    default Provider create(Provider provider) {
        return save(provider);
    }

    /**
     * Update an existing provider
     * Updates the provider entity in the database
     *
     * @param provider The provider entity to update
     * @return The updated provider entity
     */
    default Provider update(Provider provider) {
        return save(provider);
    }
}
