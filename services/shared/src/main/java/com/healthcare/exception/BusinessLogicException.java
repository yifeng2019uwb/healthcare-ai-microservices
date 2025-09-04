package com.healthcare.exception;

/**
 * Exception thrown when domain-specific business rules are violated.
 *
 * This exception is used for complex business logic violations that involve
 * domain knowledge, state transitions, and business constraints.
 *
 * Future Enhancements:
 * - Add HIPAA compliance business rules and violations
 * - Add patient consent management business logic
 * - Add medical device integration business rules
 * - Add healthcare workflow state management
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class BusinessLogicException extends HealthcareException {

    private static final String ERROR_CODE = "BUSINESS_LOGIC_ERROR";

    /**
     * Constructs a new business logic exception with the specified message.
     *
     * @param message the error message
     */
    public BusinessLogicException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new business logic exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new business logic exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public BusinessLogicException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }

    /**
     * Creates a business logic exception for appointment scheduling conflicts.
     *
     * @param providerId the provider ID
     * @param scheduledTime the requested appointment time
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException appointmentConflict(Object providerId, String scheduledTime) {
        return new BusinessLogicException("Appointment conflict: Provider '{}' is not available at {}",
            providerId, scheduledTime);
    }

    /**
     * Creates a business logic exception for invalid appointment status transitions.
     *
     * @param currentStatus the current appointment status
     * @param requestedStatus the requested appointment status
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException invalidStatusTransition(String currentStatus, String requestedStatus) {
        return new BusinessLogicException("Invalid status transition from '{}' to '{}'",
            currentStatus, requestedStatus);
    }

    /**
     * Creates a business logic exception for medical record access restrictions.
     *
     * @param recordId the medical record ID
     * @param reason the reason for access denial
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException medicalRecordAccessDenied(Object recordId, String reason) {
        return new BusinessLogicException("Access denied to medical record '{}': {}", recordId, reason);
    }

    /**
     * Creates a business logic exception for provider availability issues.
     *
     * @param providerId the provider ID
     * @param reason the reason for unavailability
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException providerUnavailable(Object providerId, String reason) {
        return new BusinessLogicException("Provider '{}' is unavailable: {}", providerId, reason);
    }

    /**
     * Creates a business logic exception for patient data privacy violations.
     *
     * @param patientId the patient ID
     * @param violation the privacy violation description
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException privacyViolation(Object patientId, String violation) {
        return new BusinessLogicException("Privacy violation for patient '{}': {}", patientId, violation);
    }

    /**
     * Creates a business logic exception for duplicate resource creation.
     *
     * @param resourceType the type of resource
     * @param identifier the duplicate identifier
     * @return a new BusinessLogicException
     */
    public static BusinessLogicException duplicateResource(String resourceType, Object identifier) {
        return new BusinessLogicException("{} with identifier '{}' already exists", resourceType, identifier);
    }
}
