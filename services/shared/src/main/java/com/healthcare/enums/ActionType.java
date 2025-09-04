package com.healthcare.enums;

/**
 * Action type enumeration for audit logs
 */
public enum ActionType {
    CREATE("CREATE", "Create operation"),
    READ("READ", "Read operation"),
    UPDATE("UPDATE", "Update operation"),
    DELETE("DELETE", "Delete operation"),
    LOGIN("LOGIN", "User login"),
    LOGOUT("LOGOUT", "User logout");

    private final String code;
    private final String description;

    ActionType(String code, String description) {
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
