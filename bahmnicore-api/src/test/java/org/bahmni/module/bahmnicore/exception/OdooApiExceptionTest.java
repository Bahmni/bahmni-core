package org.bahmni.module.bahmnicore.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OdooApiExceptionTest {

    @Test
    public void shouldCreateExceptionWithMessageOnly() {
        OdooApiException exception = new OdooApiException("Something went wrong");

        assertEquals("Something went wrong", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void shouldCreateExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        OdooApiException exception = new OdooApiException("Odoo error", cause);

        assertEquals("Odoo error", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("root cause", exception.getCause().getMessage());
    }

    @Test
    public void shouldBeInstanceOfRuntimeException() {
        OdooApiException exception = new OdooApiException("test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void shouldPreserveNestedExceptionChain() {
        IOException ioException = new IOException("IO failure");
        OdooApiException exception = new OdooApiException("Auth failed: IO failure", ioException);

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    // Helper - simulating IOException without importing java.io
    private static class IOException extends Exception {
        IOException(String message) {
            super(message);
        }
    }
}
