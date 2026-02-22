package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : EventException.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for event-related errors
 * </pre>
 */

public class EventException extends RuntimeException {
    
    public EventException(String message) {
        super(message);
    }
    
    public EventException(String message, Throwable cause) {
        super(message, cause);
    }
}