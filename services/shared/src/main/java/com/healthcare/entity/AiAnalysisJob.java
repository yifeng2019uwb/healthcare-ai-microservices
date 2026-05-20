package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.AiJobStatus;
import com.healthcare.enums.AiTriggerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Outbox table for AI analysis workflow.
 *
 * One row per patient — patient_id IS the primary key.
 * Written by ConditionService/AllergyService on clinical record saves (UPSERT).
 * Consumed by AiAnalysisScheduler every 30s.
 *
 * Lifecycle: PENDING → PROCESSING → COMPLETED | FAILED
 * COMPLETED rows purged after 24h, FAILED rows after 7d.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_AI_ANALYSIS_JOBS,
        indexes = {
            @Index(name = DatabaseConstants.INDEX_AI_JOBS_STATUS_MARKED,
                   columnList = DatabaseConstants.COL_STATUS + "," + DatabaseConstants.COL_MARKED_AT)
        })
public class AiAnalysisJob extends BaseEntity {

    @Id
    @Column(name = DatabaseConstants.COL_PATIENT_ID, updatable = false, nullable = false)
    private UUID patientId;

    @Column(name = DatabaseConstants.COL_MARKED_AT, nullable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime markedAt;

    @Column(name = DatabaseConstants.COL_TRIGGERED_BY)
    private UUID triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_TRIGGER_TYPE, nullable = false,
            length = DatabaseConstants.LEN_TRIGGER_TYPE)
    private AiTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_STATUS, nullable = false,
            length = DatabaseConstants.LEN_AI_JOB_STATUS)
    private AiJobStatus status;

    @Column(name = DatabaseConstants.COL_LOCK_EXPIRES_AT,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime lockExpiresAt;

    @Column(name = DatabaseConstants.COL_COMPLETED_AT,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime completedAt;

    @Column(name = DatabaseConstants.COL_LAST_ERROR,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String lastError;

    @Column(name = DatabaseConstants.COL_RETRY_COUNT, nullable = false)
    private int retryCount;

    @Column(name = DatabaseConstants.COL_NEXT_RETRY_AT,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime nextRetryAt;

    protected AiAnalysisJob() {}

    public AiAnalysisJob(UUID patientId, AiTriggerType triggerType, UUID triggeredBy) {
        this.patientId   = patientId;
        this.triggerType = triggerType;
        this.triggeredBy = triggeredBy;
        this.markedAt    = OffsetDateTime.now();
        this.status      = AiJobStatus.PENDING;
        this.retryCount  = 0;
    }

    public UUID getPatientId()             { return patientId; }
    public OffsetDateTime getMarkedAt()    { return markedAt; }
    public UUID getTriggeredBy()           { return triggeredBy; }
    public AiTriggerType getTriggerType()  { return triggerType; }
    public AiJobStatus getStatus()         { return status; }
    public OffsetDateTime getLockExpiresAt() { return lockExpiresAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public String getLastError()           { return lastError; }
    public int getRetryCount()             { return retryCount; }
    public OffsetDateTime getNextRetryAt() { return nextRetryAt; }

    public void setMarkedAt(OffsetDateTime markedAt)         { this.markedAt = markedAt; }
    public void setTriggeredBy(UUID triggeredBy)             { this.triggeredBy = triggeredBy; }
    public void setTriggerType(AiTriggerType triggerType)    { this.triggerType = triggerType; }
    public void setStatus(AiJobStatus status)                { this.status = status; }
    public void setLockExpiresAt(OffsetDateTime lockExpiresAt) { this.lockExpiresAt = lockExpiresAt; }
    public void setCompletedAt(OffsetDateTime completedAt)   { this.completedAt = completedAt; }
    public void setLastError(String lastError)               { this.lastError = lastError; }
    public void setRetryCount(int retryCount)                { this.retryCount = retryCount; }
    public void setNextRetryAt(OffsetDateTime nextRetryAt)   { this.nextRetryAt = nextRetryAt; }
}
