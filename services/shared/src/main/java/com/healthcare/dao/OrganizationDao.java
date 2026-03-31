package com.healthcare.dao;

import com.healthcare.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for Organization entity.
 * Maps to organizations table — owned by provider-service.
 */
@Repository
public interface OrganizationDao extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByName(String name);

    List<Organization> findByCity(String city);

    List<Organization> findByState(String state);

    boolean existsByName(String name);
}