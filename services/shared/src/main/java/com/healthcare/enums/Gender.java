package com.healthcare.enums;

/**
 * Gender enumeration for user profiles
 */
public enum Gender {
    MALE("MALE", "Male"),
    FEMALE("FEMALE", "Female"),
    OTHER("OTHER", "Other"),
    UNKNOWN("UNKNOWN", "Unknown");

    private final String code;
    private final String description;

    Gender(String code, String description) {
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
