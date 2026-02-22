package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : CalendarController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for calendar operations
 * </pre>
 */

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.CalendarViewRequest;
import com.bgc.event.dto.TimeSlotDto;
import com.bgc.event.exception.EventException;
import com.bgc.event.security.CurrentUser;
import com.bgc.event.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    
    private final CalendarService calendarService;
    
    /**
     * Public calendar endpoint - no authentication required
     */
    @GetMapping("/public/events")
    public ResponseEntity<List<CalendarEventDto>> getPublicCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("Public calendar request from {} to {}", start, end);
        
        CalendarViewRequest request = CalendarViewRequest.builder()
                .start(start)
                .end(end)
                .publicView(true)
                .build();
        
        List<CalendarEventDto> events = calendarService.getCalendarEvents(request);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Protected calendar endpoint for authenticated users
     */
    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long organizerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        log.info("Protected calendar request from {} to {}", start, end);
        
        CalendarViewRequest request = CalendarViewRequest.builder()
                .start(start)
                .end(end)
                .organizerId(organizerId)
                .status(status)
                .search(search)
                .publicView(false)
                .build();
        
        List<CalendarEventDto> events = calendarService.getCalendarEvents(request);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventDto> getCalendarEventDetails(@PathVariable Long eventId) 
            throws EventException {
        
        log.info("Request for calendar event details: {}", eventId);
        CalendarEventDto event = calendarService.getCalendarEventDetails(eventId);
        return ResponseEntity.ok(event);
    }
    
    @GetMapping("/status-colors")
    public ResponseEntity<Map<String, String>> getStatusColorMapping() {
        log.info("Request for status color mapping");
        return ResponseEntity.ok(calendarService.getStatusColorMapping());
    }
    
    @PutMapping("/events/{eventId}/move")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<CalendarEventDto> moveEvent(
            @PathVariable Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEnd,
            @CurrentUser Long userId) throws EventException {
        
        log.info("Moving event ID: {} to new start: {}", eventId, newStart);
        
        CalendarEventDto updatedEvent = calendarService.updateEventFromCalendar(
                eventId, newStart, newEnd, userId);
        
        return ResponseEntity.ok(updatedEvent);
    }
    
    @GetMapping("/events/{eventId}/timeslots")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<TimeSlotDto>> getAvailableTimeSlots(@PathVariable Long eventId) {
        log.info("Getting available time slots for event: {}", eventId);
        List<TimeSlotDto> slots = calendarService.getAvailableTimeSlots(eventId);
        return ResponseEntity.ok(slots);
    }
    
    @GetMapping("/export/ical")
    public ResponseEntity<String> exportToICal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long organizerId) {
        
        log.info("Exporting calendar to iCal from {} to {}", start, end);
        
        CalendarViewRequest request = CalendarViewRequest.builder()
                .start(start)
                .end(end)
                .organizerId(organizerId)
                .build();
        
        String icalData = calendarService.exportToICal(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "bgc-events.ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icalData);
    }
}