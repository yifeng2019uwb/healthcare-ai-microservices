package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
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
@Table(name = DatabaseConstants.TABLE_AUDIT_LOGS)
public class AuditLog extends BaseEntity {

    /**
     * Foreign key linking to user_profiles.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = DatabaseConstants.COL_USER_ID, nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ACTION_TYPE, nullable = false)
    private ActionType actionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_RESOURCE_TYPE, nullable = false)
    private ResourceType resourceType;

    @Column(name = DatabaseConstants.COL_RESOURCE_ID)
    private UUID resourceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_OUTCOME, nullable = false)
    private Outcome outcome;

    @Column(name = DatabaseConstants.COL_DETAILS, columnDefinition = "JSONB")
    private String details;

    @Column(name = DatabaseConstants.COL_SOURCE_IP, columnDefinition = "INET")
    private InetAddress sourceIp;

    @Size(max = 500)
    @Column(name = DatabaseConstants.COL_USER_AGENT, columnDefinition = "TEXT")
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


}
