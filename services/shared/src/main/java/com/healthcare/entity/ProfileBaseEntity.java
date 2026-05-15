package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/**
 * Extends BaseEntity with an auto-generated UUID primary key.
 *
 * Extended by all entities that have a single UUID PK managed by the database:
 *   User, Patient, Provider, Organization, Encounter, AuditLog
 *
 * Entities with composite PKs (Condition, Allergy) extend BaseEntity directly
 * and define their own @EmbeddedId.
 */
@MappedSuperclass
public abstract class ProfileBaseEntity extends BaseEntity {

    /**
     * UUID primary key — set by the service layer before save.
     * For new records: service calls UUID.randomUUID().
     * For Synthea import: service uses the Synthea UUID to preserve FK links.
     * DB enforces uniqueness only — refuses duplicate UUIDs on insert.
     */
    @Id
    @Column(name = DatabaseConstants.COL_ID,
            updatable = false,
            nullable = false,
            columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    protected ProfileBaseEntity() {}

    // ------------------------------------------------------------------
    // Getter / Setter
    // ------------------------------------------------------------------

    public UUID getId() { return id; }

    /**
     * Set the UUID before calling save(). Write-once — throws if already set.
     * Use UUID.randomUUID() for new records, or the Synthea UUID for import.
     */
    public void setId(UUID id) {
        if (this.id != null)
            throw new ValidationException("ID is already set and cannot be changed");
        this.id = id;
    }
}