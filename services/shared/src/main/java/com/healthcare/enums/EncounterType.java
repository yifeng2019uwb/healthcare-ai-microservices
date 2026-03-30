package com.healthcare.enums;

/**
 * Encounter type enumeration
 */
public enum EncounterType {
    REGULAR_CONSULTATION,
    FOLLOW_UP,
    NEW_PATIENT_INTAKE,
    PROCEDURE_CONSULTATION;

    /**
     * Get duration in minutes for each appointment type
     */
    public int getDurationMinutes() {
        return switch (this) {
            case REGULAR_CONSULTATION -> 30;
            case FOLLOW_UP -> 15;
            case NEW_PATIENT_INTAKE -> 60;
            case PROCEDURE_CONSULTATION -> 45;
        };
    }
}
