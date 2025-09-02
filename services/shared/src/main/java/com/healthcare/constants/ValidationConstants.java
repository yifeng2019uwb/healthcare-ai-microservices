package com.healthcare.constants;

/**
 * Validation rules and patterns for healthcare data
 */
public final class ValidationConstants {

    // String Lengths
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_ADDRESS_LENGTH = 500;
    public static final int MAX_SPECIALTY_LENGTH = 100;
    public static final int MAX_NOTES_LENGTH = 2000;

    // Patterns
    public static final String PATTERN_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PATTERN_PHONE = "^\\+?[1-9]\\d{1,14}$";
    public static final String PATTERN_UUID = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    // Business Rules
    public static final int MIN_APPOINTMENT_DURATION_MINUTES = 15;
    public static final int MAX_APPOINTMENT_DURATION_MINUTES = 480; // 8 hours
    public static final int MAX_FUTURE_APPOINTMENT_DAYS = 90;
    public static final int MIN_APPOINTMENT_ADVANCE_HOURS = 2;

    private ValidationConstants() {
        // Utility class
    }
}
