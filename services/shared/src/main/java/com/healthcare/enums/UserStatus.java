package com.healthcare.enums;

/**
 * User status enumeration for user profiles
 */
public enum UserStatus {
    ACTIVE("ACTIVE", "Active user"),
    INACTIVE("INACTIVE", "Inactive user"),
    SUSPENDED("SUSPENDED", "Suspended user");

    private final String code;
    private final String description;

    UserStatus(String code, String description) {
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
