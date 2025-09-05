package com.healthcare.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BusinessLogicException
 */
class BusinessLogicExceptionTest {

    @Test
    void testBusinessLogicExceptionBasicConstructor() {
        String message = "Business logic violation";

        BusinessLogicException exception = new BusinessLogicException(message);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testBusinessLogicExceptionWithCause() {
        String message = "Business logic violation";
        RuntimeException cause = new RuntimeException("Root cause");

        BusinessLogicException exception = new BusinessLogicException(message, cause);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testBusinessLogicExceptionWithMessageArgs() {
        String message = "Provider {} is not available at {}";
        Object[] messageArgs = {"Dr. Smith", "2025-01-10 10:00"};

        BusinessLogicException exception = new BusinessLogicException(message, messageArgs);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo("Provider Dr. Smith is not available at 2025-01-10 10:00");
    }

    @Test
    void testBusinessLogicExceptionAppointmentConflict() {
        Object providerId = "provider-123";
        String scheduledTime = "2025-01-10 10:00";

        BusinessLogicException exception = BusinessLogicException.appointmentConflict(providerId, scheduledTime);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Appointment conflict: Provider 'provider-123' is not available at 2025-01-10 10:00");
    }

    @Test
    void testBusinessLogicExceptionInvalidStatusTransition() {
        String currentStatus = "SCHEDULED";
        String requestedStatus = "CANCELLED";

        BusinessLogicException exception = BusinessLogicException.invalidStatusTransition(currentStatus, requestedStatus);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Invalid status transition from 'SCHEDULED' to 'CANCELLED'");
    }

    @Test
    void testBusinessLogicExceptionMedicalRecordAccessDenied() {
        Object recordId = "record-456";
        String reason = "Patient consent required";

        BusinessLogicException exception = BusinessLogicException.medicalRecordAccessDenied(recordId, reason);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Access denied to medical record 'record-456': Patient consent required");
    }

    @Test
    void testBusinessLogicExceptionProviderUnavailable() {
        Object providerId = "provider-789";
        String reason = "On vacation";

        BusinessLogicException exception = BusinessLogicException.providerUnavailable(providerId, reason);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Provider 'provider-789' is unavailable: On vacation");
    }

    @Test
    void testBusinessLogicExceptionPrivacyViolation() {
        Object patientId = "patient-101";
        String violation = "Unauthorized access attempt";

        BusinessLogicException exception = BusinessLogicException.privacyViolation(patientId, violation);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Privacy violation for patient 'patient-101': Unauthorized access attempt");
    }

    @Test
    void testBusinessLogicExceptionDuplicateResource() {
        String resourceType = "User";
        Object identifier = "john.doe@example.com";

        BusinessLogicException exception = BusinessLogicException.duplicateResource(resourceType, identifier);

        assertThat(exception.getErrorCode()).isEqualTo("BUSINESS_LOGIC_ERROR");
        assertThat(exception.getMessage()).isEqualTo("User with identifier 'john.doe@example.com' already exists");
    }

    @Test
    void testBusinessLogicExceptionInheritance() {
        BusinessLogicException exception = new BusinessLogicException("Test message");

        assertThat(exception).isInstanceOf(HealthcareException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
