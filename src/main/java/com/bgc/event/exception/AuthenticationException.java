package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : AuthenticationException.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for authentication errors
 * </pre>
 */

public class AuthenticationException extends Exception {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}