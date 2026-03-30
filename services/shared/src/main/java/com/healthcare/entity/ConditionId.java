package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


/**
 * Composite primary key for Condition entity.
 * Maps to conditions table PK: (patient_id, encounter_id, code)
 */
@Embeddable
public class ConditionId implements Serializable {

    @Column(name = DatabaseConstants.COL_PATIENT_ID)
    private UUID patientId;

    @Column(name = DatabaseConstants.COL_ENCOUNTER_ID)
    private UUID encounterId;

    @Column(name = DatabaseConstants.COL_CODE)
    private String code;

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    /** For JPA only. */
    public ConditionId() {}

    public ConditionId(UUID patientId, UUID encounterId, String code) {
        this.patientId   = patientId;
        this.encounterId = encounterId;
        this.code        = code;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public UUID getPatientId()   { return patientId; }
    public UUID getEncounterId() { return encounterId; }
    public String getCode()      { return code; }

    // ------------------------------------------------------------------
    // equals and hashCode — required for composite PK
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConditionId)) return false;
        ConditionId that = (ConditionId) o;
        return Objects.equals(patientId, that.patientId)
                && Objects.equals(encounterId, that.encounterId)
                && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, encounterId, code);
    }
}