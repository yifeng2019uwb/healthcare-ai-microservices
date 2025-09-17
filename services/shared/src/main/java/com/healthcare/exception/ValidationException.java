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

}
