package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.ResourceType;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Audit log entity for tracking system activities
 * Maps to audit_logs table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_AUDIT_LOGS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_AUDIT_LOGS_USER_ACTIVITY, columnList = DatabaseConstants.COL_USER_ID + "," + DatabaseConstants.COL_CREATED_AT),
           @Index(name = DatabaseConstants.INDEX_AUDIT_LOGS_RESOURCE_ACTIVITY, columnList = DatabaseConstants.COL_RESOURCE_TYPE + "," + DatabaseConstants.COL_RESOURCE_ID + "," + DatabaseConstants.COL_CREATED_AT),
           @Index(name = DatabaseConstants.INDEX_AUDIT_LOGS_SECURITY_MONITORING, columnList = DatabaseConstants.COL_ACTION_TYPE + "," + DatabaseConstants.COL_OUTCOME + "," + DatabaseConstants.COL_CREATED_AT)
       })
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_DETAILS, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode details;

    @Column(name = DatabaseConstants.COL_SOURCE_IP, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_INET)
    private InetAddress sourceIp;

    @Column(name = DatabaseConstants.COL_USER_AGENT, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String userAgent;

    // ==================== CONSTRUCTORS ====================

    /**
     * Private constructor for JPA only.
     */
    @SuppressWarnings("unused")
    private AuditLog() {}

    /**
     * Simple constructor for required fields only.
     * Use setters for optional fields.
     *
     * @param userId The ID of the user performing the action
     * @param actionType The type of action performed
     * @param resourceType The type of resource affected
     * @param outcome The outcome of the action
     */
    public AuditLog(UUID userId, ActionType actionType, ResourceType resourceType, Outcome outcome) {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (actionType == null) {
            throw new ValidationException("Action type is required");
        }
        if (resourceType == null) {
            throw new ValidationException("Resource type is required");
        }
        if (outcome == null) {
            throw new ValidationException("Outcome is required");
        }

        this.userId = userId;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.outcome = outcome;
    }


    // Getters and Setters

    public UUID getUserId() {
        return userId;
    }

    // Note: userId is immutable after creation - use factory methods to create new audit logs

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


    public JsonNode getDetails() {
        return details;
    }

    public void setDetails(JsonNode details) {
        this.details = details;
    }

    public InetAddress getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(InetAddress sourceIp) {
        this.sourceIp = validateSourceIp(sourceIp);
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }


    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = this.validateUserAgent(userAgent);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates the source IP address.
     * If not null, ensures it's a valid IP address format.
     *
     * @param sourceIp the IP address to validate
     * @return the validated IP address or null
     * @throws ValidationException if the IP address format is invalid
     */
    private InetAddress validateSourceIp(InetAddress sourceIp) {
        if (sourceIp == null) {
            return null;
        }

        // InetAddress.getByName() already validates the format, so if we have an InetAddress object,
        // it's already valid. We only reject multicast addresses as they're not valid source IPs.
        // Loopback addresses are valid for development, testing, and local environments.

        if (sourceIp.isMulticastAddress()) {
            throw new ValidationException("Source IP cannot be a multicast address");
        }

        return sourceIp;
    }

    private String validateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return null;
        }

        String trimmed = userAgent.trim();
        if (trimmed.length() > 500) {
            throw new ValidationException("User agent cannot exceed 500 characters");
        }

        return trimmed;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validates that the audit log object is in a valid state.
     * This should be called after object creation to ensure all required fields are set.
     *
     * @throws ValidationException if the audit log is in an invalid state
     */
    public void validateState() {
        if (userId == null) {
            throw new ValidationException("User ID is required");
        }
        if (actionType == null) {
            throw new ValidationException("Action type is required");
        }
        if (resourceType == null) {
            throw new ValidationException("Resource type is required");
        }
        if (outcome == null) {
            throw new ValidationException("Outcome is required");
        }
    }

    /**
     * Checks if this audit log has security details recorded.
     *
     * @return true if both source IP and user agent are recorded
     */
    public boolean hasSecurityDetails() {
        return sourceIp != null && userAgent != null && !userAgent.trim().isEmpty();
    }

}
