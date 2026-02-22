package com.bgc.event.exception;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.exception
 * - File       : AttendanceException.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom exception for attendance-related errors
 * </pre>
 */

public class AttendanceException extends Exception {
    
    public AttendanceException(String message) {
        super(message);
    }
    
    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
    }
}