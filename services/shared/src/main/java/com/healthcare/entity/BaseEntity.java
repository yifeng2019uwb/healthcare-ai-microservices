package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;



/**
 * Base entity providing audit fields for all healthcare entities.
 *
 * Does NOT include an id field — id management is handled by:
 *   - ProfileBaseEntity: auto-generated UUID for API-created entities
 *   - Direct @Id / @EmbeddedId: for entities with composite PKs (Condition, Allergy)
 *
 * DB level: all fields are nullable to support imported/legacy data.
 * Application level: always set by Hibernate or AuditListener.
 */
@MappedSuperclass
@EntityListeners(BaseEntity.AuditListener.class)
public abstract class BaseEntity {
    private static final String FIELD_UPDATED_BY = "Updated by";

    /**
     * Timestamp when this record was first created.
     * Set by Hibernate on INSERT via @CreationTimestamp.
     * DB column has no NOT NULL — permissive for imported data.
     */
    @CreationTimestamp
    @Column(name = DatabaseConstants.COL_CREATED_AT,
            updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime createdAt;

    /**
     * Timestamp when this record was last modified.
     * Updated by Hibernate on every UPDATE via @UpdateTimestamp.
     * DB column has no NOT NULL — permissive for imported data.
     */
    @UpdateTimestamp
    @Column(name = DatabaseConstants.COL_UPDATED_AT,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime updatedAt;

    /**
     * Username or identifier of who last modified this record.
     * Set by service layer from JWT claims for authenticated requests.
     * Falls back to AppConstants.SYSTEM_USER for automated operations.
     * DB column is nullable — permissive for imported/legacy data.
     */
    @Column(name = DatabaseConstants.COL_UPDATED_BY, length = 100)
    private String updatedBy;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------

    protected BaseEntity() {}

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy()         { return updatedBy; }

    // ------------------------------------------------------------------
    // Setter — only updatedBy is settable externally
    // ------------------------------------------------------------------

    /**
     * Set who last modified this entity.
     * Call this in the service layer with username from JWT claims.
     * Example: entity.setUpdatedBy(jwtClaims.getUsername());
     *
     * @param updatedBy username from JWT, or AppConstants.SYSTEM_USER
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = ValidationUtils.validateRequiredString(
                updatedBy, FIELD_UPDATED_BY, 100);
    }

    // ------------------------------------------------------------------
    // JPA Audit Listener
    // ------------------------------------------------------------------

    /**
     * Fallback listener — sets updatedBy to SYSTEM_USER if service layer
     * has not already set it before INSERT or UPDATE.
     */
    public static class AuditListener {

        @PrePersist
        public void prePersist(BaseEntity entity) {
            if (entity.getUpdatedBy() == null || entity.getUpdatedBy().isBlank()) {
                entity.setUpdatedBy(AppConstants.SYSTEM_USER);
            }
        }

        @PreUpdate
        public void preUpdate(BaseEntity entity) {
            if (entity.getUpdatedBy() == null || entity.getUpdatedBy().isBlank()) {
                entity.setUpdatedBy(AppConstants.SYSTEM_USER);
            }
        }
    }
}