package com.bgc.event.controller.api;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AttendanceController.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for attendance operations
 * </pre>
 */

import com.bgc.event.dto.AttendanceDto;
import com.bgc.event.dto.AttendanceSummaryDto;
import com.bgc.event.dto.CheckInRequest;
import com.bgc.event.dto.CheckInResponse;
import com.bgc.event.exception.AttendanceException;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import com.bgc.event.security.CurrentUser;
import com.bgc.event.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<CheckInResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @CurrentUser Long userId,
            HttpServletRequest httpRequest) 
            throws AttendanceException, RegistrationException, EventException {
        
        log.info("REST request to check-in attendee for event: {}", request.getEventId());
        CheckInResponse response = attendanceService.checkIn(request, userId, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/checkin/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<CheckInResponse>> bulkCheckIn(
            @RequestBody List<CheckInRequest> requests,
            @CurrentUser Long userId) {
        
        log.info("REST request for bulk check-in of {} attendees", requests.size());
        List<CheckInResponse> responses = attendanceService.bulkCheckIn(requests, userId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/events/{eventId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<AttendanceDto>> getEventAttendance(
            @PathVariable Long eventId,
            Pageable pageable) {
        
        log.info("REST request to get attendance for event: {}", eventId);
        Page<AttendanceDto> attendance = attendanceService.getEventAttendance(eventId, pageable);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/events/{eventId}/attendance/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<AttendanceSummaryDto> getAttendanceSummary(@PathVariable Long eventId) {
        log.info("REST request to get attendance summary for event: {}", eventId);
        AttendanceSummaryDto summary = attendanceService.getEventAttendanceSummary(eventId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/events/{eventId}/attendance/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceTrends(
            @PathVariable Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("REST request to get attendance trends for event: {}", eventId);
        List<Map<String, Object>> trends = attendanceService.getAttendanceTrends(eventId, startDate, endDate);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/events/{eventId}/attendance/count")
    public ResponseEntity<Map<String, Long>> getCurrentCheckInCount(@PathVariable Long eventId) {
        long count = attendanceService.getCurrentCheckInCount(eventId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/events/{eventId}/attendance/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<AttendanceDto>> getRecentCheckIns(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("REST request to get recent check-ins for event: {}", eventId);
        List<AttendanceDto> recent = attendanceService.getRecentCheckIns(eventId, limit);
        return ResponseEntity.ok(recent);
    }
    
    @GetMapping("/registrations/{registrationId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<AttendanceDto> getRegistrationAttendance(@PathVariable Long registrationId) 
            throws AttendanceException {
        
        log.info("REST request to get attendance for registration: {}", registrationId);
        AttendanceDto attendance = attendanceService.getRegistrationAttendance(registrationId);
        return ResponseEntity.ok(attendance);
    }
    
    @DeleteMapping("/attendance/{attendanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> undoCheckIn(
            @PathVariable Long attendanceId,
            @CurrentUser Long userId) throws AttendanceException {
        
        log.info("REST request to undo check-in: {}", attendanceId);
        attendanceService.undoCheckIn(attendanceId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/events/{eventId}/attendance/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> exportAttendance(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "pdf") String format) throws EventException {
        
        log.info("REST request to export attendance for event: {} as {}", eventId, format);
        byte[] data = attendanceService.exportAttendanceReport(eventId, format);
        
        String filename = String.format("attendance_event_%d.%s", eventId, format);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}