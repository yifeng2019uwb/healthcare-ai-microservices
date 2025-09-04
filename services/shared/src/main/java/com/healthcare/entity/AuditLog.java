package com.healthcare.entity;

import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.ResourceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Audit log entity for tracking system activities
 * Maps to audit_logs table
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    /**
     * Foreign key linking to user_profiles.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private Outcome outcome;

    @Column(name = "details", columnDefinition = "JSONB")
    private String details;

    @Column(name = "source_ip", columnDefinition = "INET")
    private InetAddress sourceIp;

    @Size(max = 500)
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Constructors
    public AuditLog() {}

    // Constructor for service layer (with user ID only)
    public AuditLog(UUID userId, ActionType actionType, ResourceType resourceType, UUID resourceId, Outcome outcome) {
        this.userId = userId;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.outcome = outcome;
    }

    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    public ActionType getActionType() {
        return actionType;
    }


    public ResourceType getResourceType() {
        return resourceType;
    }


    public UUID getResourceId() {
        return resourceId;
    }


    public Outcome getOutcome() {
        return outcome;
    }


    public String getDetails() {
        return details;
    }


    public InetAddress getSourceIp() {
        return sourceIp;
    }


    public String getUserAgent() {
        return userAgent;
    }


    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the audit log has a valid user.
     *
     * @return true if user is present, false otherwise
     */
    public boolean hasUser() {
        return user != null;
    }

    /**
     * Validates that the audit log has a valid action type.
     *
     * @return true if action type is present, false otherwise
     */
    public boolean hasActionType() {
        return actionType != null;
    }

    /**
     * Validates that the audit log has a valid resource type.
     *
     * @return true if resource type is present, false otherwise
     */
    public boolean hasResourceType() {
        return resourceType != null;
    }

    /**
     * Validates that the audit log has a valid outcome.
     *
     * @return true if outcome is present, false otherwise
     */
    public boolean hasOutcome() {
        return outcome != null;
    }

    /**
     * Validates that the audit log has a valid resource ID.
     *
     * @return true if resource ID is present, false otherwise
     */
    public boolean hasResourceId() {
        return resourceId != null;
    }

    /**
     * Validates that the audit log has source IP information.
     *
     * @return true if source IP is present, false otherwise
     */
    public boolean hasSourceIp() {
        return sourceIp != null;
    }

    /**
     * Validates that the audit log has user agent information.
     *
     * @return true if user agent is present, false otherwise
     */
    public boolean hasUserAgent() {
        return userAgent != null && !userAgent.trim().isEmpty();
    }

    /**
     * Validates that the audit log is complete and ready for storage.
     *
     * @return true if audit log is complete, false otherwise
     */
    public boolean isComplete() {
        return hasUser() && hasActionType() && hasResourceType() && hasOutcome();
    }

    /**
     * Validates that the audit log represents a successful operation.
     *
     * @return true if operation was successful, false otherwise
     */
    public boolean isSuccessful() {
        return hasOutcome() && outcome == Outcome.SUCCESS;
    }

    /**
     * Validates that the audit log represents a failed operation.
     *
     * @return true if operation failed, false otherwise
     */
    public boolean isFailed() {
        return hasOutcome() && outcome == Outcome.FAILURE;
    }
}
