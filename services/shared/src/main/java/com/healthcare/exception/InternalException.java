package com.healthcare.exception;

/**
 * Exception thrown for internal server errors and unexpected failures.
 *
 * This exception is used for internal server issues that are not related to
 * validation or resource availability. These are typically infrastructure
 * problems, unexpected system failures, or internal application errors.
 *
 * Maps to HTTP 500 Internal Server Error status code.
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class InternalException extends HealthcareException {

    private static final String ERROR_CODE = "INTERNAL_ERROR";

    /**
     * Constructs a new internal exception with the specified message.
     *
     * @param message the error message
     */
    public InternalException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new internal exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public InternalException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new internal exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public InternalException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }
}