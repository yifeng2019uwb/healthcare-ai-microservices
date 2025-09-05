package com.healthcare.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HealthcareException abstract class
 */
class HealthcareExceptionTest {

    // Test concrete implementation of HealthcareException
    private static class TestHealthcareException extends HealthcareException {
        public TestHealthcareException(String errorCode, String errorMessage) {
            super(errorCode, errorMessage);
        }

        public TestHealthcareException(String errorCode, String errorMessage, Throwable cause) {
            super(errorCode, errorMessage, cause);
        }

        public TestHealthcareException(String errorCode, String errorMessage, Object... messageArgs) {
            super(errorCode, errorMessage, messageArgs);
        }

        public TestHealthcareException(String errorCode, String errorMessage, Throwable cause, Object... messageArgs) {
            super(errorCode, errorMessage, cause, messageArgs);
        }
    }

    @Test
    void testHealthcareExceptionBasicConstructor() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Test error message";

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getMessageArgs()).isNull();
        assertThat(exception.getFormattedMessage()).isEqualTo(errorMessage);
    }

    @Test
    void testHealthcareExceptionWithCause() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Test error message";
        RuntimeException cause = new RuntimeException("Root cause");

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, cause);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessageArgs()).isNull();
    }

    @Test
    void testHealthcareExceptionWithMessageArgs() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Test error with {} and {}";
        Object[] messageArgs = {"arg1", "arg2"};

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, messageArgs);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(exception.getMessage()).isEqualTo("Test error with arg1 and arg2");
        assertThat(exception.getMessageArgs()).isEqualTo(messageArgs);
        assertThat(exception.getFormattedMessage()).isEqualTo("Test error with arg1 and arg2");
    }

    @Test
    void testHealthcareExceptionWithCauseAndMessageArgs() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Test error with {} and {}";
        Object[] messageArgs = {"arg1", "arg2"};
        RuntimeException cause = new RuntimeException("Root cause");

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, cause, messageArgs);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(exception.getMessage()).isEqualTo("Test error with arg1 and arg2");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessageArgs()).isEqualTo(messageArgs);
    }

    @Test
    void testHealthcareExceptionToString() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Test error message";

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage);
        String toString = exception.toString();

        assertThat(toString).contains("TestHealthcareException");
        assertThat(toString).contains("errorCode=TEST_ERROR");
        assertThat(toString).contains("message=Test error message");
    }

    @Test
    void testHealthcareExceptionMessageFormatting() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "User {} has {} appointments";
        Object[] messageArgs = {"John", 5};

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, messageArgs);

        assertThat(exception.getFormattedMessage()).isEqualTo("User John has 5 appointments");
    }

    @Test
    void testHealthcareExceptionMessageFormattingWithMultiplePlaceholders() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Field {} cannot exceed {} characters (actual: {})";
        Object[] messageArgs = {"name", 50, 75};

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, messageArgs);

        assertThat(exception.getFormattedMessage()).isEqualTo("Field name cannot exceed 50 characters (actual: 75)");
    }

    @Test
    void testHealthcareExceptionMessageFormattingWithNullArgs() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Simple message without placeholders";

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, (Object[]) null);

        assertThat(exception.getFormattedMessage()).isEqualTo("Simple message without placeholders");
    }

    @Test
    void testHealthcareExceptionMessageFormattingWithEmptyArgs() {
        String errorCode = "TEST_ERROR";
        String errorMessage = "Simple message without placeholders";
        Object[] emptyArgs = {};

        TestHealthcareException exception = new TestHealthcareException(errorCode, errorMessage, emptyArgs);

        assertThat(exception.getFormattedMessage()).isEqualTo("Simple message without placeholders");
    }
}
