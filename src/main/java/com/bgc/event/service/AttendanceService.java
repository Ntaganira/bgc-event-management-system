package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : AttendanceService.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for attendance tracking operations (FR-16, FR-17, FR-18, FR-19)
 * </pre>
 */

import com.bgc.event.dto.AttendanceDto;
import com.bgc.event.dto.AttendanceSummaryDto;
import com.bgc.event.dto.CheckInRequest;
import com.bgc.event.dto.CheckInResponse;
import com.bgc.event.exception.AttendanceException;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    
    /**
     * Check-in an attendee (FR-16, FR-17)
     */
    CheckInResponse checkIn(CheckInRequest request, Long userId, HttpServletRequest httpRequest) 
            throws AttendanceException, RegistrationException, EventException;
    
    /**
     * Bulk check-in attendees
     */
    List<CheckInResponse> bulkCheckIn(List<CheckInRequest> requests, Long userId);
    
    /**
     * Get attendance by ID
     */
    AttendanceDto getAttendanceById(Long attendanceId) throws AttendanceException;
    
    /**
     * Get attendance for an event (FR-18)
     */
    Page<AttendanceDto> getEventAttendance(Long eventId, Pageable pageable);
    
    /**
     * Get attendance for a registration
     */
    AttendanceDto getRegistrationAttendance(Long registrationId) throws AttendanceException;
    
    /**
     * Get attendance summary for an event
     */
    AttendanceSummaryDto getEventAttendanceSummary(Long eventId);
    
    /**
     * Get attendance trends
     */
    List<Map<String, Object>> getAttendanceTrends(Long eventId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get check-in methods distribution
     */
    Map<String, Long> getCheckInMethodDistribution(Long eventId);
    
    /**
     * Get real-time check-in count (FR-18)
     */
    long getCurrentCheckInCount(Long eventId);
    
    /**
     * Validate if attendee can check-in
     */
    boolean canCheckIn(Long registrationId);
    
    /**
     * Undo check-in (admin only)
     */
    void undoCheckIn(Long attendanceId, Long userId) throws AttendanceException;
    
    /**
     * Export attendance report
     */
    byte[] exportAttendanceReport(Long eventId, String format) throws EventException;
    
    /**
     * Get recent check-ins
     */
    List<AttendanceDto> getRecentCheckIns(Long eventId, int limit);
}