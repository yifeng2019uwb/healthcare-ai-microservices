package com.healthcare.enums;

/**
 * Medical record types
 */
public enum MedicalRecordType {
    CONSULTATION("CONSULTATION", "Consultation notes"),
    DIAGNOSIS("DIAGNOSIS", "Diagnosis information"),
    PRESCRIPTION("PRESCRIPTION", "Prescription details"),
    LAB_RESULT("LAB_RESULT", "Laboratory results"),
    IMAGING("IMAGING", "Imaging results"),
    VITAL_SIGNS("VITAL_SIGNS", "Vital signs data");

    private final String code;
    private final String description;

    MedicalRecordType(String code, String description) {
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
