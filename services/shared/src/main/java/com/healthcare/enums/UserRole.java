package com.healthcare.enums;

/**
 * User roles in the healthcare system
 */
public enum UserRole {
    PATIENT("PATIENT", "Patient user"),
    PROVIDER("PROVIDER", "Healthcare provider");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
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
