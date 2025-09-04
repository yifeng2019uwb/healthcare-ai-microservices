package com.healthcare.exception;

/**
 * Exception thrown for system-level errors and unexpected failures.
 *
 * This exception is used for system-level issues that are not related to
 * validation, business logic, or resource availability. These are typically
 * infrastructure problems or unexpected system failures.
 *
 * Future Enhancements:
 * - Add healthcare-specific system errors (EHR integration failures)
 * - Add medical device communication errors
 * - Add HIPAA audit system failures
 * - Add external service integration errors
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class SystemException extends HealthcareException {

    private static final String ERROR_CODE = "SYSTEM_ERROR";

    /**
     * Constructs a new system exception with the specified message.
     *
     * @param message the error message
     */
    public SystemException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new system exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public SystemException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new system exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public SystemException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }

    /**
     * Creates a system exception for database connection failures.
     *
     * @param cause the underlying cause
     * @return a new SystemException
     */
    public static SystemException databaseConnectionFailed(Throwable cause) {
        return new SystemException("Database connection failed", cause);
    }

    /**
     * Creates a system exception for external service failures.
     *
     * @param serviceName the name of the external service
     * @param cause the underlying cause
     * @return a new SystemException
     */
    public static SystemException externalServiceFailed(String serviceName, Throwable cause) {
        return new SystemException("External service '{}' is unavailable", serviceName, cause);
    }

    /**
     * Creates a system exception for configuration errors.
     *
     * @param configKey the configuration key that caused the error
     * @return a new SystemException
     */
    public static SystemException configurationError(String configKey) {
        return new SystemException("Configuration error for key '{}'", configKey);
    }

    /**
     * Creates a system exception for unexpected internal errors.
     *
     * @param cause the underlying cause
     * @return a new SystemException
     */
    public static SystemException internalError(Throwable cause) {
        return new SystemException("An unexpected internal error occurred", cause);
    }

    /**
     * Creates a system exception for database constraint violations.
     *
     * @param constraintName the name of the violated constraint
     * @param cause the underlying cause
     * @return a new SystemException
     */
    public static SystemException databaseConstraintViolation(String constraintName, Throwable cause) {
        return new SystemException("Database constraint '{}' violation", constraintName, cause);
    }

    /**
     * Creates a system exception for appointment scheduling constraint violations.
     *
     * @param cause the underlying cause
     * @return a new SystemException
     */
    public static SystemException appointmentSchedulingConstraintViolation(Throwable cause) {
        return new SystemException("Appointment scheduling constraint violation: must be scheduled at least 1 day in advance", cause);
    }
}
