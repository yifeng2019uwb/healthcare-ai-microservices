package com.healthcare.enums;

/**
 * Audit action types for tracking system activities
 */
public enum AuditAction {
    CREATE("CREATE", "Record created"),
    READ("READ", "Record accessed"),
    UPDATE("UPDATE", "Record updated"),
    DELETE("DELETE", "Record deleted"),
    LOGIN("LOGIN", "User login"),
    LOGOUT("LOGOUT", "User logout");

    private final String code;
    private final String description;

    AuditAction(String code, String description) {
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
