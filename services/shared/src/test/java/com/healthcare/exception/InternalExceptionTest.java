package com.healthcare.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InternalException
 */
class InternalExceptionTest {

    @Test
    void testInternalExceptionWithMessage() {
        String message = "Database connection failed";
        InternalException exception = new InternalException(message);
        
        assertEquals("INTERNAL_ERROR", exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInternalExceptionWithMessageAndCause() {
        String message = "External service unavailable";
        Throwable cause = new RuntimeException("Connection timeout");
        InternalException exception = new InternalException(message, cause);
        
        assertEquals("INTERNAL_ERROR", exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInternalExceptionWithMessageAndArgs() {
        String template = "Configuration error for key '{}'";
        String configKey = "database.url";
        InternalException exception = new InternalException(template, configKey);
        
        assertEquals("INTERNAL_ERROR", exception.getErrorCode());
        assertEquals("Configuration error for key 'database.url'", exception.getMessage());
        assertEquals(template, exception.getErrorMessage());
        assertArrayEquals(new Object[]{configKey}, exception.getMessageArgs());
    }

    @Test
    void testInternalExceptionInheritance() {
        InternalException exception = new InternalException("Test message");
        
        assertInstanceOf(HealthcareException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testToString() {
        InternalException exception = new InternalException("System failure");
        String toString = exception.toString();
        
        assertTrue(toString.contains("InternalException"));
        assertTrue(toString.contains("INTERNAL_ERROR"));
        assertTrue(toString.contains("System failure"));
    }
}
