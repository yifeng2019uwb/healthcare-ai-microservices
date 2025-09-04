package com.healthcare.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * This exception is used when a client requests a resource (entity, record, etc.)
 * that does not exist in the system.
 *
 * Future Enhancements:
 * - Add healthcare-specific resource types (medical devices, lab results)
 * - Add HIPAA-compliant error messages for sensitive resources
 * - Add audit logging for resource access attempts
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public class ResourceNotFoundException extends HealthcareException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Constructs a new resource not found exception with the specified message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new resource not found exception with the specified message and cause.
     *
     * @param message the error message
     * @param cause the cause of this exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }

    /**
     * Constructs a new resource not found exception with the specified message and arguments.
     *
     * @param message the error message template
     * @param messageArgs the arguments to format the error message
     */
    public ResourceNotFoundException(String message, Object... messageArgs) {
        super(ERROR_CODE, message, messageArgs);
    }

    /**
     * Creates a resource not found exception for a specific entity type and ID.
     *
     * @param entityType the type of entity (e.g., "User", "Patient", "Appointment")
     * @param entityId the ID of the entity that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException entityNotFound(String entityType, Object entityId) {
        return new ResourceNotFoundException("{} with ID '{}' not found", entityType, entityId);
    }

    /**
     * Creates a resource not found exception for a user profile.
     *
     * @param userId the user ID that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException userNotFound(Object userId) {
        return entityNotFound("User", userId);
    }

    /**
     * Creates a resource not found exception for a patient profile.
     *
     * @param patientId the patient ID that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException patientNotFound(Object patientId) {
        return entityNotFound("Patient", patientId);
    }

    /**
     * Creates a resource not found exception for a provider profile.
     *
     * @param providerId the provider ID that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException providerNotFound(Object providerId) {
        return entityNotFound("Provider", providerId);
    }

    /**
     * Creates a resource not found exception for an appointment.
     *
     * @param appointmentId the appointment ID that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException appointmentNotFound(Object appointmentId) {
        return entityNotFound("Appointment", appointmentId);
    }

    /**
     * Creates a resource not found exception for a medical record.
     *
     * @param recordId the medical record ID that was not found
     * @return a new ResourceNotFoundException
     */
    public static ResourceNotFoundException medicalRecordNotFound(Object recordId) {
        return entityNotFound("Medical Record", recordId);
    }
}
