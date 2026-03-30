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
 * Allergy entity mapping to the allergies table.
 *
 * Represents a patient allergy or intolerance recorded during an encounter.
 * Uses composite PK (patient_id, encounter_id, code) — no surrogate id.
 * Extends BaseEntity for audit fields only.
 *
 * Source: Synthea allergies.csv
 * Supports up to two reactions per allergy (Synthea limitation).
 * Additional reactions can be recorded in the notes field.
 */
@Entity
@Table(name = DatabaseConstants.TABLE_ALLERGIES,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_ALLERGIES_PATIENT,
                  columnList = DatabaseConstants.COL_PATIENT_ID),
           @Index(name = DatabaseConstants.INDEX_ALLERGIES_CATEGORY,
                  columnList = DatabaseConstants.COL_CATEGORY)
       })
public class Allergy extends BaseEntity {

    @EmbeddedId
    private AllergyId id;

    @Column(name = DatabaseConstants.COL_START_DATE)
    private LocalDate startDate;

    /** Null means allergy is still active. */
    @Column(name = DatabaseConstants.COL_STOP_DATE)
    private LocalDate stopDate;

    @Size(max = DatabaseConstants.LEN_SYSTEM)
    @Column(name = DatabaseConstants.COL_SYSTEM)
    private String system;

    @Size(max = DatabaseConstants.LEN_DESCRIPTION)
    @Column(name = DatabaseConstants.COL_DESCRIPTION)
    private String description;

    /** allergy or intolerance. */
    @Size(max = DatabaseConstants.LEN_ALLERGY_TYPE)
    @Column(name = DatabaseConstants.COL_ALLERGY_TYPE)
    private String allergyType;

    /** environment, food, drug. */
    @Size(max = DatabaseConstants.LEN_CATEGORY)
    @Column(name = DatabaseConstants.COL_CATEGORY)
    private String category;

    // Reaction 1
    @Size(max = DatabaseConstants.LEN_REACTION)
    @Column(name = DatabaseConstants.COL_REACTION1)
    private String reaction1;

    @Size(max = DatabaseConstants.LEN_DESCRIPTION)
    @Column(name = DatabaseConstants.COL_DESCRIPTION1)
    private String description1;

    /** MILD, MODERATE, SEVERE. */
    @Size(max = DatabaseConstants.LEN_SEVERITY)
    @Column(name = DatabaseConstants.COL_SEVERITY1)
    private String severity1;

    // Reaction 2
    @Size(max = DatabaseConstants.LEN_REACTION)
    @Column(name = DatabaseConstants.COL_REACTION2)
    private String reaction2;

    @Size(max = DatabaseConstants.LEN_DESCRIPTION)
    @Column(name = DatabaseConstants.COL_DESCRIPTION2)
    private String description2;

    /** MILD, MODERATE, SEVERE. */
    @Size(max = DatabaseConstants.LEN_SEVERITY)
    @Column(name = DatabaseConstants.COL_SEVERITY2)
    private String severity2;

    /** Additional reactions or clinical observations beyond reaction1/2. */
    @Column(name = DatabaseConstants.COL_NOTES,
            columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String notes;

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
    private Allergy() {}

    /**
     * Create a new allergy record.
     *
     * @param id        composite PK (patient_id, encounter_id, code)
     * @param startDate date allergy was recorded
     */
    public Allergy(AllergyId id, LocalDate startDate) {
        this.id        = id;
        this.startDate = startDate;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public AllergyId getId()        { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getStopDate()  { return stopDate; }
    public String getSystem()       { return system; }
    public String getDescription()  { return description; }
    public String getAllergyType()   { return allergyType; }
    public String getCategory()     { return category; }
    public String getReaction1()    { return reaction1; }
    public String getDescription1() { return description1; }
    public String getSeverity1()    { return severity1; }
    public String getReaction2()    { return reaction2; }
    public String getDescription2() { return description2; }
    public String getSeverity2()    { return severity2; }
    public String getNotes()        { return notes; }
    public Patient getPatient()     { return patient; }
    public Encounter getEncounter() { return encounter; }

    /** Convenience getter — delegates to composite PK. */
    public String getCode()         { return id != null ? id.getCode() : null; }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }
    public void setStopDate(LocalDate stopDate)     { this.stopDate = stopDate; }
    public void setSystem(String system)            { this.system = system; }
    public void setDescription(String description)  { this.description = description; }
    public void setAllergyType(String allergyType)  { this.allergyType = allergyType; }
    public void setCategory(String category)        { this.category = category; }
    public void setReaction1(String reaction1)      { this.reaction1 = reaction1; }
    public void setDescription1(String description1) { this.description1 = description1; }
    public void setSeverity1(String severity1)      { this.severity1 = severity1; }
    public void setReaction2(String reaction2)      { this.reaction2 = reaction2; }
    public void setDescription2(String description2) { this.description2 = description2; }
    public void setSeverity2(String severity2)      { this.severity2 = severity2; }
    public void setNotes(String notes)              { this.notes = notes; }

    // ------------------------------------------------------------------
    // Business methods
    // ------------------------------------------------------------------

    /** Returns true if the allergy is still active. */
    public boolean isActive() {
        return stopDate == null;
    }
}