package com.healthcare.enums;

/**
 * Medical record types
 */
public enum MedicalRecordType {
    DIAGNOSIS("DIAGNOSIS", "Diagnosis information"),
    TREATMENT("TREATMENT", "Treatment details"),
    SUMMARY("SUMMARY", "Visit summary"),
    LAB_RESULT("LAB_RESULT", "Laboratory results"),
    PRESCRIPTION("PRESCRIPTION", "Prescription details"),
    NOTE("NOTE", "Clinical notes"),
    OTHER("OTHER", "Other medical records");

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
