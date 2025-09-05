package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base entity providing common fields for all healthcare entities.
 *
 * This abstract class provides standardized ID and audit fields that are automatically
 * managed by Hibernate and the database layer. It uses timezone-aware timestamps
 * (TIMESTAMPTZ) to ensure accurate time tracking across different time zones.
 *
 * Fields:
 * - id - UUID primary key, auto-generated (read-only)
 * - createdAt - Automatically set on entity creation (read-only)
 * - updatedAt - Automatically updated on entity modification (read-only)
 * - updatedBy - Automatically populated from JWT context via AuditListener
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2024-01-15
 */
@MappedSuperclass
@EntityListeners(BaseEntity.AuditListener.class)
public abstract class BaseEntity {

    /**
     * Primary key identifier for the entity.
     * Auto-generated UUID that uniquely identifies each record.
     * This field is read-only and cannot be modified after creation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = DatabaseConstants.COL_ID, updatable = false, nullable = false)
    private UUID id;

    /**
     * Timestamp when the entity was created.
     * Automatically managed by Hibernate @CreationTimestamp annotation.
     * This field is read-only and cannot be modified after creation.
     */
    @CreationTimestamp
    @Column(name = DatabaseConstants.COL_CREATED_AT, nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    /**
     * Timestamp when the entity was last updated.
     * Automatically managed by Hibernate @UpdateTimestamp annotation.
     * This field is read-only and is updated on every save operation.
     */
    @UpdateTimestamp
    @Column(name = DatabaseConstants.COL_UPDATED_AT, nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    /**
     * Identifier of the user who last updated this entity.
     * Automatically populated from JWT context via AuditListener on entity
     * creation and updates. Falls back to "system" if JWT context is unavailable.
     * This field is required for audit compliance in healthcare systems.
     */
    @Column(name = DatabaseConstants.COL_UPDATED_BY, length = 100, nullable = false)
    private String updatedBy;

    /**
     * Default constructor for JPA entity instantiation.
     * Protected to prevent direct instantiation of this abstract class.
     */
    protected BaseEntity() {
        // Default constructor for JPA
    }

    // ==================== GETTERS ====================

    /**
     * Returns the unique identifier for this entity.
     *
     * @return the entity ID, never null
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the timestamp when this entity was created.
     *
     * @return the creation timestamp, never null
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp when this entity was last updated.
     *
     * @return the last update timestamp, never null
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns the identifier of the user who last updated this entity.
     *
     * @return the user identifier, may be null if not set
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    // ==================== SETTERS ====================

    /**
     * Sets the identifier of the user who last updated this entity.
     * This method should be called in the service layer with the current
     * user's ID extracted from the JWT token context.
     *
     * @param updatedBy the user identifier, typically from JWT claims
     * @throws ValidationException if updatedBy is null or exceeds length limit
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = ValidationUtils.validateRequiredString(
            updatedBy,
            "Updated by",
            100
        );
    }

    // ==================== ENTITY METHODS ====================

    /**
     * Checks if this entity is active.
     * Default implementation returns true. Override in subclasses that have status fields.
     *
     * @return true if entity is active, false otherwise
     */
    public boolean isActive() {
        return true;
    }

    // ==================== AUDIT LISTENER ====================

    /**
     * JPA Entity Listener for automatic audit field population.
     * Automatically sets updatedBy from JWT context on entity creation and updates.
     */
    public static class AuditListener {

        /**
         * Automatically populate updatedBy before entity creation.
         * Called by JPA before INSERT operations.
         */
        @PrePersist
        public void prePersist(BaseEntity entity) {
            String currentUserId = getCurrentUserIdFromJWT();
            entity.setUpdatedBy(currentUserId);
        }

        /**
         * Automatically populate updatedBy before entity update.
         * Called by JPA before UPDATE operations.
         */
        @PreUpdate
        public void preUpdate(BaseEntity entity) {
            String currentUserId = getCurrentUserIdFromJWT();
            entity.setUpdatedBy(currentUserId);
        }

        /**
         * Extract current user ID from JWT context.
         * This method should be implemented to integrate with your authentication system.
         *
         * @return the current user ID from JWT token, or null if not available
         */
        private String getCurrentUserIdFromJWT() {
            try {
                // TODO: Implement JWT context extraction
                // This is a placeholder - replace with actual JWT integration
                // Example implementations:

                // Option 1: Spring Security Context
                // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                // if (auth != null && auth.getPrincipal() instanceof JwtAuthenticationToken) {
                //     JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) auth.getPrincipal();
                //     return jwtToken.getToken().getClaimAsString("sub");
                // }

                // Option 2: Custom JWT Service
                // return jwtContextService.getCurrentUserId();

                // Option 3: Request Context
                // return requestContextHolder.getCurrentUserId();

                return "system"; // Fallback for now
            } catch (Exception e) {
                // Log error but don't fail the operation
                // logger.warn("Failed to extract user ID from JWT context", e);
                return "system";
            }
        }
    }
}
