package com.verif.core.exception;

/**
 * Custom runtime exception representing domain-specific verification failures,
 * duplicate registrations, or illegal lifecycle transitions.
 */
public class VerificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new VerificationException with the specified error message.
     * 
     * @param message Human-readable failure explanation
     */
    public VerificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new VerificationException with message and underlying cause.
     * 
     * @param message Human-readable failure explanation
     * @param cause   Root throwable cause
     */
    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
