package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : UserException.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for user-related errors
 * </pre>
 */

public class UserException extends Exception {
    
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}