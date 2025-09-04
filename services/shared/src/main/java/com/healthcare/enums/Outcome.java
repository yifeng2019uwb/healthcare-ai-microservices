package com.healthcare.enums;

/**
 * Outcome enumeration for audit logs
 */
public enum Outcome {
    SUCCESS("SUCCESS", "Operation successful"),
    FAILURE("FAILURE", "Operation failed");

    private final String code;
    private final String description;

    Outcome(String code, String description) {
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
