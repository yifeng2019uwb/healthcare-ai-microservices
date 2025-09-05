package com.healthcare.constants;

/**
 * Centralized validation pattern constants for healthcare entities.
 *
 * This class provides reusable regex patterns for consistent validation
 * across all entities, avoiding hardcoded patterns and ensuring
 * maintainability.
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2024-01-15
 */
public final class ValidationPatterns {

    // Private constructor to prevent instantiation
    private ValidationPatterns() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== CONTACT PATTERNS ====================

    /**
     * International phone number pattern.
     * Supports formats like: +1234567890, +44123456789, 1234567890
     * Must start with + or digit 1-9, followed by 1-14 digits
     */
    public static final String PHONE = "^\\+?[1-9]\\d{1,14}$";

    /**
     * Email address pattern.
     * Supports standard email formats with domain validation
     */
    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";

    // ==================== ADDRESS PATTERNS ====================

    /**
     * Postal code pattern.
     * Supports various international postal code formats:
     * - US: 12345, 12345-6789
     * - UK: SW1A 1AA, M1 1AA
     * - Canada: K1A 0A6
     * - General: 3-20 characters with letters, numbers, spaces, or hyphens
     */
    public static final String POSTAL_CODE = "^[A-Za-z0-9\\s-]{3,20}$";

    /**
     * Street address pattern.
     * Supports various address formats with numbers, letters, spaces, and common separators
     */
    public static final String STREET_ADDRESS = "^[A-Za-z0-9\\s.,#-]{5,255}$";

    /**
     * City name pattern.
     * Supports international city names with letters, spaces, hyphens, periods, and apostrophes.
     * Examples: "St. Petersburg", "San Francisco-Oakland", "O'Connor", "New York"
     * Minimum 2 characters, maximum 100 characters
     */
    public static final String CITY_NAME = "^[\\p{L}\\s.'-]{2,100}$";

    /**
     * State/Province name pattern.
     * Supports state and province names with letters, spaces, hyphens, and periods.
     * Examples: "New York", "British Columbia", "St. Louis", "São Paulo"
     * Minimum 2 characters, maximum 50 characters
     */
    public static final String STATE_NAME = "^[\\p{L}\\s.'-]{2,50}$";

    /**
     * Country name pattern.
     * Supports international country names with letters, spaces, hyphens, and periods.
     * Examples: "United States", "United Kingdom", "Côte d'Ivoire", "St. Kitts and Nevis"
     * Minimum 2 characters, maximum 100 characters
     */
    public static final String COUNTRY_NAME = "^[\\p{L}\\s.'-]{2,100}$";

    // ==================== IDENTIFICATION PATTERNS ====================

    /**
     * NPI (National Provider Identifier) pattern.
     * 10-digit numeric identifier for healthcare providers
     */
    public static final String NPI = "^\\d{10}$";

    /**
     * Patient number pattern.
     * Alphanumeric identifier with specific format
     */
    public static final String PATIENT_NUMBER = "^[A-Z]{2}\\d{8}$";

    /**
     * External authentication ID pattern.
     * Supports various external auth provider formats
     */
    public static final String EXTERNAL_AUTH_ID = "^[A-Za-z0-9._-]{3,255}$";

    // ==================== MEDICAL PATTERNS ====================

    /**
     * Medical record number pattern.
     * Format: MR followed by 8 digits
     */
    public static final String MEDICAL_RECORD_NUMBER = "^MR\\d{8}$";

    /**
     * Insurance policy number pattern.
     * Alphanumeric with specific format
     */
    public static final String INSURANCE_POLICY = "^[A-Z]{2}\\d{10}$";

    // ==================== NAME PATTERNS ====================

    /**
     * Person name pattern for healthcare industry compliance.
     * Supports international names with Unicode letters, spaces, hyphens, and apostrophes.
     * Based on healthcare industry standards and FHIR HumanName requirements.
     * Allows: letters (including international/Unicode), spaces, hyphens, apostrophes
     * Minimum 2 characters, maximum 100 characters
     */
    public static final String PERSON_NAME = "^[\\p{L}\\s'-]{2,100}$";

    /**
     * Organization name pattern.
     * Supports business names with letters, numbers, spaces, and common business characters
     */
    public static final String ORGANIZATION_NAME = "^[A-Za-z0-9\\s&.,'-]{2,255}$";

    // ==================== DATE/TIME PATTERNS ====================

    /**
     * Date pattern (YYYY-MM-DD).
     * ISO 8601 date format
     */
    public static final String DATE_ISO = "^\\d{4}-\\d{2}-\\d{2}$";

    /**
     * Time pattern (HH:MM:SS).
     * 24-hour time format
     */
    public static final String TIME_24H = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";

    // ==================== HEALTHCARE SPECIFIC PATTERNS ====================

    /**
     * ICD-10 diagnosis code pattern.
     * Format: Letter followed by 2-3 digits, optional decimal and 1-2 more digits
     */
    public static final String ICD10_CODE = "^[A-Z]\\d{2,3}(\\.\\d{1,2})?$";

    /**
     * CPT procedure code pattern.
     * 5-digit numeric code for medical procedures
     */
    public static final String CPT_CODE = "^\\d{5}$";

    /**
     * Drug NDC (National Drug Code) pattern.
     * Format: 5-4-2 digit segments separated by hyphens
     */
    public static final String NDC_CODE = "^\\d{5}-\\d{4}-\\d{2}$";

    // ==================== GENERAL PATTERNS ====================

    /**
     * Alphanumeric with spaces pattern.
     * For general text fields that allow letters, numbers, and spaces
     */
    public static final String ALPHANUMERIC_SPACES = "^[A-Za-z0-9\\s]{1,255}$";

    /**
     * Alphanumeric without spaces pattern.
     * For codes and identifiers that don't allow spaces
     */
    public static final String ALPHANUMERIC_NO_SPACES = "^[A-Za-z0-9]{1,255}$";

    /**
     * UUID pattern.
     * Standard UUID format validation
     */
    public static final String UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
}
