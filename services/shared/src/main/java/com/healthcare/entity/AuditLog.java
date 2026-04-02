package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Size;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AuditLog entity mapping to the audit_logs table.
 *
 * HIPAA Security Rule 45 CFR § 164.312(b) — records all access to ePHI.
 * Append only — never updated or deleted.
 * Retention: minimum 6 years per HIPAA requirement.
 *
 * Does NOT extend BaseEntity or ProfileBaseEntity —
 * audit_logs has no updated_at or updated_by columns.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_AUDIT_LOGS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_AUDIT_AUTH_ID,
                  columnList = DatabaseConstants.COL_AUTH_ID),
           @Index(name = DatabaseConstants.INDEX_AUDIT_RESOURCE,
                  columnList = DatabaseConstants.COL_RESOURCE_TYPE + "," + DatabaseConstants.COL_RESOURCE_ID),
           @Index(name = DatabaseConstants.INDEX_AUDIT_CREATED_AT,
                  columnList = DatabaseConstants.COL_CREATED_AT)
       })
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = DatabaseConstants.COL_ID,
            updatable = false,
            nullable = false,
            columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    // WHO
    /** User identity — null for system actions. */
    @Size(max = DatabaseConstants.LEN_AUDIT_AUTH_ID)
    @Column(name = DatabaseConstants.COL_AUTH_ID)
    private String authId;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_USER_ROLE)
    private UserRole userRole;

    // WHAT
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ACTION)
    private ActionType action;

    @Size(max = DatabaseConstants.LEN_RESOURCE_TYPE)
    @Column(name = DatabaseConstants.COL_RESOURCE_TYPE)
    private String resourceType;

    @Column(name = DatabaseConstants.COL_RESOURCE_ID)
    private UUID resourceId;

    // OUTCOME
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_OUTCOME)
    private Outcome outcome;

    // WHERE FROM
    @Column(name = DatabaseConstants.COL_SOURCE_IP,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_INET)
    private InetAddress sourceIp;

    @Column(name = DatabaseConstants.COL_USER_AGENT,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String userAgent;

    // WHEN
    @CreationTimestamp
    @Column(name = DatabaseConstants.COL_CREATED_AT,
            updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime createdAt;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    protected AuditLog() {}

    /**
     * Create an audit log entry.
     *
     * @param action       READ, CREATE, UPDATE
     * @param resourceType e.g. patients, encounters, conditions
     * @param outcome      SUCCESS or FAILURE
     */
    public AuditLog(ActionType action, String resourceType, Outcome outcome) {
        this.action       = action;
        this.resourceType = resourceType;
        this.outcome      = outcome;
    }

    // ------------------------------------------------------------------
    // Getters — no setters, append only
    // ------------------------------------------------------------------

    public UUID getId()              { return id; }
    public String getAuthId()        { return authId; }
    public UserRole getUserRole()    { return userRole; }
    public ActionType getAction()    { return action; }
    public String getResourceType()  { return resourceType; }
    public UUID getResourceId()      { return resourceId; }
    public Outcome getOutcome()      { return outcome; }
    public InetAddress getSourceIp() { return sourceIp; }
    public String getUserAgent()     { return userAgent; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    // ------------------------------------------------------------------
    // Builder-style setters — called before first save only
    // ------------------------------------------------------------------

    public AuditLog withAuthId(String authId) {
        this.authId = authId;
        return this;
    }

    public AuditLog withUserRole(UserRole userRole) {
        this.userRole = userRole;
        return this;
    }

    public AuditLog withAction(ActionType action) {
        this.action = action;
        return this;
    }

    public AuditLog withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public AuditLog withResourceId(UUID resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public AuditLog withOutcome(Outcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public AuditLog withSourceIp(InetAddress sourceIp) {
        this.sourceIp = sourceIp;
        return this;
    }

    public AuditLog withUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}