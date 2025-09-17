package com.healthcare.exception;

/**
 * Exception thrown when a resource conflict occurs.
 *
 * This exception is used when a client attempts to create or modify a resource
 * in a way that conflicts with the current state of the system, such as trying
 * to create a resource that already exists.
 *
 * Maps to HTTP 409 Conflict status code.
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class ConflictException extends HealthcareException {

    private static final String ERROR_CODE = "CONFLICT_ERROR";

    /**
     * Constructs a new conflict exception with the specified message.
     *
     * @param message the error message
     */
    public ConflictException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new conflict exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public ConflictException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new conflict exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public ConflictException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }
}
