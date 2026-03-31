package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.EncounterStatus;
import com.healthcare.enums.EncounterType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Encounter entity mapping to the encounters table.
 *
 * Represents a clinical encounter between a patient and provider.
 * Previously referred to as "appointment" in the old design —
 * renamed to Encounter to match Synthea/FHIR terminology and the actual table name.
 *
 * organization_id is a snapshot at time of encounter —
 * provider/org may change after the encounter is recorded.
 *
 * payer_id is stored as VARCHAR (no FK) — payers table is Phase 2.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_ENCOUNTERS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_ENCOUNTERS_PATIENT,
                  columnList = DatabaseConstants.COL_PATIENT_ID),
           @Index(name = DatabaseConstants.INDEX_ENCOUNTERS_PROVIDER,
                  columnList = DatabaseConstants.COL_PROVIDER_ID),
           @Index(name = DatabaseConstants.INDEX_ENCOUNTERS_START_TIME,
                  columnList = DatabaseConstants.COL_START_TIME)
       })
public class Encounter extends ProfileBaseEntity {

    @Column(name = DatabaseConstants.COL_PATIENT_ID)
    private UUID patientId;

    @Column(name = DatabaseConstants.COL_PROVIDER_ID)
    private UUID providerId;

    @Column(name = DatabaseConstants.COL_ORGANIZATION_ID)
    private UUID organizationId;

    /** Phase 2: will become FK when payers table is added. */
    @Size(max = DatabaseConstants.LEN_PAYER_ID)
    @Column(name = DatabaseConstants.COL_PAYER_ID)
    private String payerId;

    @Column(name = DatabaseConstants.COL_START_TIME,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime startTime;

    @Column(name = DatabaseConstants.COL_STOP_TIME,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime stopTime;

    @Size(max = DatabaseConstants.LEN_ENCOUNTER_CLASS)
    @Column(name = DatabaseConstants.COL_ENCOUNTER_CLASS)
    private String encounterClass;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ENCOUNTER_STATUS,
            length = DatabaseConstants.LEN_ENCOUNTER_STATUS)
    private EncounterStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_ENCOUNTER_TYPE,
            length = DatabaseConstants.LEN_ENCOUNTER_TYPE)
    private EncounterType encounterType;

    @Size(max = DatabaseConstants.LEN_CODE)
    @Column(name = DatabaseConstants.COL_CODE)
    private String code;

    @Size(max = DatabaseConstants.LEN_DESCRIPTION)
    @Column(name = DatabaseConstants.COL_DESCRIPTION)
    private String description;

    @Column(name = DatabaseConstants.COL_BASE_COST,
        precision = DatabaseConstants.LEN_COST_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE)
    private BigDecimal baseCost;

    @Column(name = DatabaseConstants.COL_TOTAL_COST,
        precision = DatabaseConstants.LEN_COST_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE)
    private BigDecimal totalCost;

    /** Phase 2: related to payer billing. */
    @Column(name = DatabaseConstants.COL_PAYER_COVERAGE,
        precision = DatabaseConstants.LEN_COST_PRECISION,
        scale = DatabaseConstants.LEN_MONEY_SCALE)
    private BigDecimal payerCoverage;

    @Size(max = DatabaseConstants.LEN_REASON_CODE)
    @Column(name = DatabaseConstants.COL_REASON_CODE)
    private String reasonCode;

    @Size(max = DatabaseConstants.LEN_REASON_DESC)
    @Column(name = DatabaseConstants.COL_REASON_DESC)
    private String reasonDesc;

    // ------------------------------------------------------------------
    // JPA relationships
    // ------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_PATIENT_ID,
                insertable = false, updatable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_PROVIDER_ID,
                insertable = false, updatable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_ORGANIZATION_ID,
                insertable = false, updatable = false)
    private Organization organization;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    private Encounter() {}

    /**
     * Create a new encounter.
     * Business rule validation (e.g. patientId required when booked)
     * is handled by the service layer.
     *
     * @param providerId     the provider UUID
     * @param organizationId the organization UUID
     * @param startTime      when the encounter starts
     */
    public Encounter(UUID providerId, OffsetDateTime startTime) {
        this.providerId = providerId;
        this.startTime  = startTime;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public UUID getPatientId()                  { return patientId; }
    public UUID getProviderId()                 { return providerId; }
    public UUID getOrganizationId()             { return organizationId; }
    public String getPayerId()                  { return payerId; }
    public OffsetDateTime getStartTime()        { return startTime; }
    public OffsetDateTime getStopTime()         { return stopTime; }
    public String getEncounterClass()           { return encounterClass; }
    public EncounterStatus getStatus()   {return status;}
    public EncounterType getEncounterType()     {return encounterType;}
    public String getCode()                     { return code; }
    public String getDescription()              { return description; }
    public BigDecimal getBaseCost()             { return baseCost; }
    public BigDecimal getTotalCost()            { return totalCost; }
    public BigDecimal getPayerCoverage()        { return payerCoverage; }
    public String getReasonCode()               { return reasonCode; }
    public String getReasonDesc()               { return reasonDesc; }
    public Patient getPatient()                 { return patient; }
    public Provider getProvider()               { return provider; }
    public Organization getOrganization()       { return organization; }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    public void setPatientId(UUID patientId)                    { this.patientId = patientId; }
    public void setProviderId(UUID providerId)                  { this.providerId = providerId; }
    public void setOrganizationId(UUID organizationId)          { this.organizationId = organizationId; }
    public void setStartTime(OffsetDateTime startTime)          { this.startTime = startTime; }
    public void setPayerId(String payerId)                      { this.payerId = payerId; }
    public void setStopTime(OffsetDateTime stopTime)            { this.stopTime = stopTime; }
    public void setEncounterClass(String encounterClass)        { this.encounterClass = encounterClass; }
    public void setStatus(EncounterStatus status)               {this.status = status;}
    public void setEncounterType(EncounterType encounterType)   {this.encounterType = encounterType;}
    public void setCode(String code)                            { this.code = code; }
    public void setDescription(String description)              { this.description = description; }
    public void setBaseCost(BigDecimal baseCost)                { this.baseCost = baseCost; }
    public void setTotalCost(BigDecimal totalCost)              { this.totalCost = totalCost; }
    public void setPayerCoverage(BigDecimal payerCoverage)      { this.payerCoverage = payerCoverage; }
    public void setReasonCode(String reasonCode)                { this.reasonCode = reasonCode; }
    public void setReasonDesc(String reasonDesc)                { this.reasonDesc = reasonDesc; }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    public boolean isCompleted() {
        return stopTime != null;
    }
}