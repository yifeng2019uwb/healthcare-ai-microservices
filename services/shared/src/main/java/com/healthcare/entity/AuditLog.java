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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public AuditLog(User user, ActionType actionType, ResourceType resourceType, UUID resourceId, Outcome outcome) {
        this.user = user;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.outcome = outcome;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public InetAddress getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(InetAddress sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
