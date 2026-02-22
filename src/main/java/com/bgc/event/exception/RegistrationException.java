package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : RegistrationException.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for registration-related errors
 * </pre>
 */

public class RegistrationException extends Exception {
    
    public RegistrationException(String message) {
        super(message);
    }
    
    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}