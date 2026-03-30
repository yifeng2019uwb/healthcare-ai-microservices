package com.healthcare.entity;

import com.healthcare.constants.AppConstants;
import com.healthcare.constants.DatabaseConstants;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Condition entity mapping to the conditions table.
 *
 * Represents a clinical condition diagnosed during an encounter.
 * Uses composite PK (patient_id, encounter_id, code) — no surrogate id.
 * Extends BaseEntity for audit fields only.
 *
 * Source: Synthea conditions.csv
 * SYSTEM defaults to SNOMED-CT but kept flexible for future data sources.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_CONDITIONS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_CONDITIONS_PATIENT,
                  columnList = DatabaseConstants.COL_PATIENT_ID),
           @Index(name = DatabaseConstants.INDEX_CONDITIONS_CODE,
                  columnList = DatabaseConstants.COL_CODE)
       })
public class Condition extends BaseEntity {

    @EmbeddedId
    private ConditionId id;

    @Column(name = DatabaseConstants.COL_START_DATE)
    private LocalDate startDate;

    /** Null means ongoing condition. */
    @Column(name = DatabaseConstants.COL_STOP_DATE)
    private LocalDate stopDate;

    @Size(max = DatabaseConstants.LEN_SYSTEM)
    @Column(name = DatabaseConstants.COL_SYSTEM)
    private String system;

    @Size(max = DatabaseConstants.LEN_DESCRIPTION)
    @Column(name = DatabaseConstants.COL_DESCRIPTION)
    private String description;

    // ------------------------------------------------------------------
    // JPA relationships
    // ------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId(AppConstants.FIELD_PATIENT_ID)
    @JoinColumn(name = DatabaseConstants.COL_PATIENT_ID)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId(AppConstants.FIELD_ENCOUNTER_ID)
    @JoinColumn(name = DatabaseConstants.COL_ENCOUNTER_ID)
    private Encounter encounter;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    @SuppressWarnings(AppConstants.SUPPRESS_UNUSED)
    private Condition() {}

    /**
     * Create a new condition.
     *
     * @param id        composite PK (patient_id, encounter_id, code)
     * @param startDate date condition was diagnosed
     */
    public Condition(ConditionId id, LocalDate startDate) {
        this.id        = id;
        this.startDate = startDate;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public ConditionId getId()      { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getStopDate()  { return stopDate; }
    public String getSystem()       { return system; }
    public String getDescription()  { return description; }
    public Patient getPatient()     { return patient; }
    public Encounter getEncounter() { return encounter; }

    /** Convenience getter — delegates to composite PK. */
    public String getCode()         { return id != null ? id.getCode() : null; }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }
    public void setStopDate(LocalDate stopDate)      { this.stopDate = stopDate; }
    public void setSystem(String system)             { this.system = system; }
    public void setDescription(String description)   { this.description = description; }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    /** Returns true if the condition is still ongoing. */
    public boolean isOngoing() {
        return stopDate == null;
    }
}