package com.healthcare.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConflictException
 */
class ConflictExceptionTest {

    @Test
    void testConflictExceptionWithMessage() {
        String message = "Resource already exists";
        ConflictException exception = new ConflictException(message);

        assertEquals("CONFLICT_ERROR", exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConflictExceptionWithMessageAndCause() {
        String message = "Resource already exists";
        Throwable cause = new RuntimeException("Database constraint violation");
        ConflictException exception = new ConflictException(message, cause);

        assertEquals("CONFLICT_ERROR", exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConflictExceptionWithMessageAndArgs() {
        String template = "User with email '{}' already exists";
        String email = "test@example.com";
        ConflictException exception = new ConflictException(template, email);

        assertEquals("CONFLICT_ERROR", exception.getErrorCode());
        assertEquals("User with email 'test@example.com' already exists", exception.getMessage());
        assertEquals(template, exception.getErrorMessage());
        assertArrayEquals(new Object[]{email}, exception.getMessageArgs());
    }

    @Test
    void testConflictExceptionInheritance() {
        ConflictException exception = new ConflictException("Test message");

        assertInstanceOf(HealthcareException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testToString() {
        ConflictException exception = new ConflictException("Resource conflict");
        String toString = exception.toString();

        assertTrue(toString.contains("ConflictException"));
        assertTrue(toString.contains("CONFLICT_ERROR"));
        assertTrue(toString.contains("Resource conflict"));
    }
}
