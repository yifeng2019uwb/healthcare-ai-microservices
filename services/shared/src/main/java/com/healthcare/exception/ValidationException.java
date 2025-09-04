package com.healthcare.exception;

/**
 * Exception thrown when input data validation fails.
 *
 * This exception is used for basic input validation failures that can be
 * resolved by the client providing correct data format or required fields.
 *
 * Future Enhancements:
 * - Add healthcare-specific validation (medical record numbers, insurance IDs)
 * - Add HIPAA-compliant data validation rules
 * - Add medical device data format validation
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class ValidationException extends HealthcareException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    /**
     * Constructs a new validation exception with the specified message.
     *
     * @param message the error message
     */
    public ValidationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new validation exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new validation exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public ValidationException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }


    /**
     * Creates a validation exception for a required field that is missing.
     *
     * @param fieldName the name of the required field
     * @return a new ValidationException
     */
    public static ValidationException requiredField(String fieldName) {
        return new ValidationException("Required field '{}' is missing", fieldName);
    }

    /**
     * Creates a validation exception for an invalid field value.
     *
     * @param fieldName the name of the field
     * @param value the invalid value
     * @return a new ValidationException
     */
    public static ValidationException invalidValue(String fieldName, Object value) {
        return new ValidationException("Invalid value '{}' for field '{}'", value, fieldName);
    }

    /**
     * Creates a validation exception for a field that exceeds maximum length.
     *
     * @param fieldName the name of the field
     * @param maxLength the maximum allowed length
     * @param actualLength the actual length
     * @return a new ValidationException
     */
    public static ValidationException maxLengthExceeded(String fieldName, int maxLength, int actualLength) {
        return new ValidationException("Field '{}' exceeds maximum length of {} characters (actual: {})",
            fieldName, maxLength, actualLength);
    }
}
