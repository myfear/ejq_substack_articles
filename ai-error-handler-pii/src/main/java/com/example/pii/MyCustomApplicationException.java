package com.example.pii;

/**
 * Custom application exception used to represent application-level errors.
 * <p>
 * This exception extends {@link RuntimeException} and is designed to be caught
 * by the custom exception handler ({@link MyCustomExceptionHandler}) which
 * processes the exception message and may contain PII that needs to be redacted
 * before being returned to the client or logged.
 * </p>
 * <p>
 * The exception message may contain Personally Identifiable Information (PII)
 * such as names, email addresses, or other sensitive data, which should be
 * handled appropriately by the error handling mechanism.
 * </p>
 */
public class MyCustomApplicationException extends RuntimeException {
    
    /**
     * Constructs a new custom application exception with the specified detail message.
     *
     * @param message the detail message, which may contain PII that will be
     *                processed by the exception handler
     */
    public MyCustomApplicationException(String message) {
        super(message);
    }
}