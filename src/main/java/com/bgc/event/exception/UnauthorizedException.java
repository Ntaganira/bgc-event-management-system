package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : UnauthorizedException.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for authorization errors
 * </pre>
 */

public class UnauthorizedException extends Exception {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}