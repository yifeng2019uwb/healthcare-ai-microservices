package com.healthcare.enums;

/**
 * Appointment status values
 */
public enum AppointmentStatus {
    SCHEDULED("SCHEDULED", "Appointment scheduled"),
    CONFIRMED("CONFIRMED", "Appointment confirmed"),
    IN_PROGRESS("IN_PROGRESS", "Appointment in progress"),
    COMPLETED("COMPLETED", "Appointment completed"),
    CANCELLED("CANCELLED", "Appointment cancelled"),
    NO_SHOW("NO_SHOW", "Patient did not show");

    private final String code;
    private final String description;

    AppointmentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
