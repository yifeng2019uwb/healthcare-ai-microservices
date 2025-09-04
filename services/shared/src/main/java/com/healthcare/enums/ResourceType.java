package com.healthcare.enums;

/**
 * Resource type enumeration for audit logs
 */
public enum ResourceType {
    USER_PROFILE("USER_PROFILE", "User profile"),
    PATIENT_PROFILE("PATIENT_PROFILE", "Patient profile"),
    PROVIDER_PROFILE("PROVIDER_PROFILE", "Provider profile"),
    APPOINTMENT("APPOINTMENT", "Appointment"),
    MEDICAL_RECORD("MEDICAL_RECORD", "Medical record");

    private final String code;
    private final String description;

    ResourceType(String code, String description) {
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
