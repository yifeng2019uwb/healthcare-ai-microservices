package com.healthcare.utils;

import com.healthcare.constants.ValidationPatterns;
import com.healthcare.exception.ValidationException;

/**
 * Utility class for common validation patterns used across entities
 */
public class ValidationUtils {

    /**
     * Validates and normalizes a string field with the common pattern:
     * 1. Trim whitespace
     * 2. Normalize blank/empty to null
     * 3. Validate length if maxLength is provided
     * 4. Validate pattern if pattern is provided
     * 5. Return the processed value
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength maximum allowed length (null if no length validation)
     * @param pattern regex pattern to validate against (null if no pattern validation)
     * @param patternErrorMessage error message for pattern validation failure
     * @return the processed value (trimmed or null if blank)
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeString(String value, String fieldName,
                                                   Integer maxLength, String pattern, String patternErrorMessage) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (value.isBlank()) {
            return null;  // Normalize to NULL
        }

        if (maxLength != null && value.length() > maxLength) {
            throw new ValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }

        if (pattern != null && !value.matches(pattern)) {
            throw new ValidationException(patternErrorMessage);
        }

        return value;
    }

    /**
     * Validates and normalizes a string field with only length validation
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength maximum allowed length
     * @return the processed value (trimmed or null if blank)
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeString(String value, String fieldName, int maxLength) {
        return validateAndNormalizeString(value, fieldName, maxLength, null, null);
    }

    /**
     * Validates and normalizes a string field with length and pattern validation
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength maximum allowed length
     * @param pattern regex pattern to validate against
     * @param patternErrorMessage error message for pattern validation failure
     * @return the processed value (trimmed or null if blank)
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeString(String value, String fieldName, int maxLength,
                                                   String pattern, String patternErrorMessage) {
        return validateAndNormalizeString(value, fieldName, Integer.valueOf(maxLength), pattern, patternErrorMessage);
    }

    /**
     * Validates a required string field (cannot be null or empty after trimming)
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength maximum allowed length
     * @param pattern regex pattern to validate against
     * @param patternErrorMessage error message for pattern validation failure
     * @return the processed value (trimmed)
     * @throws ValidationException if validation fails
     */
    public static String validateRequiredString(String value, String fieldName, int maxLength,
                                              String pattern, String patternErrorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be null or empty");
        }

        value = value.trim();

        if (value.length() > maxLength) {
            throw new ValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }

        if (pattern != null && !value.matches(pattern)) {
            throw new ValidationException(patternErrorMessage);
        }

        return value;
    }

    /**
     * Validates a required string field with only length validation
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param maxLength maximum allowed length
     * @return the processed value (trimmed)
     * @throws ValidationException if validation fails
     */
    public static String validateRequiredString(String value, String fieldName, int maxLength) {
        return validateRequiredString(value, fieldName, maxLength, null, null);
    }

    /**
     * Validates and normalizes a string field with only pattern validation
     *
     * @param value the input value to validate
     * @param fieldName the name of the field for error messages
     * @param pattern regex pattern to validate against
     * @param patternErrorMessage error message for pattern validation failure
     * @return the processed value (trimmed or null if blank)
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeStringWithPattern(String value, String fieldName,
                                                             String pattern, String patternErrorMessage) {
        return validateAndNormalizeString(value, fieldName, null, pattern, patternErrorMessage);
    }
}
