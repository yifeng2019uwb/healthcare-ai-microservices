package com.healthcare.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidationException
 */
class ValidationExceptionTest {

    @Test
    void testValidationExceptionBasicConstructor() {
        String message = "Validation failed";

        ValidationException exception = new ValidationException(message);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void testValidationExceptionWithCause() {
        String message = "Validation failed";
        RuntimeException cause = new RuntimeException("Root cause");

        ValidationException exception = new ValidationException(message, cause);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void testValidationExceptionWithMessageArgs() {
        String message = "Field {} is required";
        Object[] messageArgs = {"email"};

        ValidationException exception = new ValidationException(message, messageArgs);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getErrorMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).isEqualTo("Field email is required");
    }

    @Test
    void testValidationExceptionRequiredField() {
        String template = "Required field '{}' is missing";
        String fieldName = "email";

        ValidationException exception = new ValidationException(template, fieldName);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Required field 'email' is missing");
    }

    @Test
    void testValidationExceptionInvalidValue() {
        String template = "Invalid value '{}' for field '{}'";
        String fieldName = "age";
        Object value = -5;

        ValidationException exception = new ValidationException(template, value, fieldName);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Invalid value '-5' for field 'age'");
    }

    @Test
    void testValidationExceptionMaxLengthExceeded() {
        String template = "Field '{}' exceeds maximum length of {} characters (actual: {})";
        String fieldName = "description";
        int maxLength = 100;
        int actualLength = 150;

        ValidationException exception = new ValidationException(template, fieldName, maxLength, actualLength);

        assertThat(exception.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Field 'description' exceeds maximum length of 100 characters (actual: 150)");
    }

    @Test
    void testValidationExceptionInheritance() {
        ValidationException exception = new ValidationException("Test message");

        assertThat(exception).isInstanceOf(HealthcareException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
