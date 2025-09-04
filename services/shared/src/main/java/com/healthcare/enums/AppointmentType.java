package com.healthcare.enums;

/**
 * Appointment type enumeration
 */
public enum AppointmentType {
    REGULAR_CONSULTATION("REGULAR_CONSULTATION", "Regular consultation (30 minutes)"),
    FOLLOW_UP("FOLLOW_UP", "Follow-up appointment (15 minutes)"),
    NEW_PATIENT_INTAKE("NEW_PATIENT_INTAKE", "New patient intake (60 minutes)"),
    PROCEDURE_CONSULTATION("PROCEDURE_CONSULTATION", "Procedure consultation (45 minutes)");

    private final String code;
    private final String description;

    AppointmentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

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
