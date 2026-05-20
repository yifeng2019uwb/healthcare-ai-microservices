package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.AiTriggerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Append-only AI analysis results table.
 *
 * Immutable after insert — never updated.
 * Each Gemini call produces one new row; history is preserved.
 * Governance fields (triggerType, triggeredBy, modelVersion, inputRecordIds)
 * are required for auditability and explainability.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_AI_ANALYSIS_RESULTS,
        indexes = {
            @Index(name = DatabaseConstants.INDEX_AI_RESULTS_PATIENT_ID,
                   columnList = DatabaseConstants.COL_PATIENT_ID),
            @Index(name = DatabaseConstants.INDEX_AI_RESULTS_GENERATED_AT,
                   columnList = DatabaseConstants.COL_GENERATED_AT)
        })
public class AiAnalysisResult extends ProfileBaseEntity {

    @Column(name = DatabaseConstants.COL_PATIENT_ID, nullable = false, updatable = false)
    private UUID patientId;

    @Column(name = DatabaseConstants.COL_GENERATED_AT, nullable = false, updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime generatedAt;

    @Column(name = DatabaseConstants.COL_AI_SUMMARY, nullable = false, updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String summary;

    /** JSON-serialized list of risk flag objects: [{flag, reason}] */
    @Column(name = DatabaseConstants.COL_RISK_FLAGS, nullable = false, updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private String riskFlags;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_TRIGGER_TYPE, nullable = false, updatable = false,
            length = DatabaseConstants.LEN_TRIGGER_TYPE)
    private AiTriggerType triggerType;

    @Column(name = DatabaseConstants.COL_TRIGGERED_BY, updatable = false)
    private UUID triggeredBy;

    @Column(name = DatabaseConstants.COL_MODEL_VERSION, nullable = false, updatable = false,
            length = DatabaseConstants.LEN_MODEL_VERSION)
    private String modelVersion;

    /** JSON-serialized list of condition/allergy UUIDs included in this Gemini call */
    @Column(name = DatabaseConstants.COL_INPUT_RECORD_IDS, nullable = false, updatable = false,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private String inputRecordIds;

    protected AiAnalysisResult() {}

    public AiAnalysisResult(UUID patientId, String summary, String riskFlags,
                             AiTriggerType triggerType, UUID triggeredBy,
                             String modelVersion, String inputRecordIds) {
        this.patientId       = patientId;
        this.generatedAt     = OffsetDateTime.now();
        this.summary         = summary;
        this.riskFlags       = riskFlags;
        this.triggerType     = triggerType;
        this.triggeredBy     = triggeredBy;
        this.modelVersion    = modelVersion;
        this.inputRecordIds  = inputRecordIds;
    }

    public UUID getPatientId()             { return patientId; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public String getSummary()             { return summary; }
    public String getRiskFlags()           { return riskFlags; }
    public AiTriggerType getTriggerType()  { return triggerType; }
    public UUID getTriggeredBy()           { return triggeredBy; }
    public String getModelVersion()        { return modelVersion; }
    public String getInputRecordIds()      { return inputRecordIds; }
}
