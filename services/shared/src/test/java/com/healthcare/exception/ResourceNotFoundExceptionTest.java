package com.healthcare.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResourceNotFoundException
 */
class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundExceptionBasicConstructor() {
        String message = "Resource not found";

        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testResourceNotFoundExceptionWithCause() {
        String message = "Resource not found";
        RuntimeException cause = new RuntimeException("Root cause");

        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testResourceNotFoundExceptionWithMessageArgs() {
        String message = "{} with ID '{}' not found";
        Object[] messageArgs = {"User", "123"};

        ResourceNotFoundException exception = new ResourceNotFoundException(message, messageArgs);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo("User with ID '123' not found");
    }

    @Test
    void testResourceNotFoundExceptionWithMessageTemplate() {
        String template = "{} with ID '{}' not found";
        String entityType = "Patient";
        Object entityId = "456";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, entityType, entityId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("Patient with ID '456' not found");
    }

    @Test
    void testResourceNotFoundExceptionUserNotFound() {
        String template = "User with ID '{}' not found";
        Object userId = "user-123";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, userId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("User with ID 'user-123' not found");
    }

    @Test
    void testResourceNotFoundExceptionPatientNotFound() {
        String template = "Patient with ID '{}' not found";
        Object patientId = "patient-456";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, patientId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("Patient with ID 'patient-456' not found");
    }

    @Test
    void testResourceNotFoundExceptionProviderNotFound() {
        String template = "Provider with ID '{}' not found";
        Object providerId = "provider-789";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, providerId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("Provider with ID 'provider-789' not found");
    }

    @Test
    void testResourceNotFoundExceptionAppointmentNotFound() {
        String template = "Appointment with ID '{}' not found";
        Object appointmentId = "appointment-101";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, appointmentId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("Appointment with ID 'appointment-101' not found");
    }

    @Test
    void testResourceNotFoundExceptionMedicalRecordNotFound() {
        String template = "Medical Record with ID '{}' not found";
        Object recordId = "record-202";

        ResourceNotFoundException exception = new ResourceNotFoundException(template, recordId);

        assertThat(exception.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("Medical Record with ID 'record-202' not found");
    }

    @Test
    void testResourceNotFoundExceptionInheritance() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");

        assertThat(exception).isInstanceOf(HealthcareException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
