package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : EventController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for event management endpoints
 * </pre>
 */

import com.bgc.event.dto.EventCreateRequest;
import com.bgc.event.dto.EventDto;
import com.bgc.event.dto.EventStatistics;
import com.bgc.event.dto.EventUpdateRequest;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.UnauthorizedException;
import com.bgc.event.security.CurrentUser;
import com.bgc.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDto> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @CurrentUser Long userId) {
        log.info("REST request to create event by user ID: {}", userId);
        EventDto result = eventService.createEvent(request, userId);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
    
    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request,
            @CurrentUser Long userId) throws EventException, UnauthorizedException {
        log.info("REST request to update event ID: {}", eventId);
        EventDto result = eventService.updateEvent(eventId, request, userId);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            @CurrentUser Long userId) throws EventException, UnauthorizedException {
        log.info("REST request to delete event ID: {}", eventId);
        eventService.deleteEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long eventId) throws EventException {
        log.info("REST request to get event ID: {}", eventId);
        EventDto result = eventService.getEventById(eventId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    public ResponseEntity<Page<EventDto>> getAllEvents(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("REST request to get all events, page: {}", pageable);
        Page<EventDto> result = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<EventDto>> getEventsByOrganizer(
            @PathVariable Long organizerId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get events for organizer ID: {}", organizerId);
        Page<EventDto> result = eventService.getEventsByOrganizer(organizerId, pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EventDto>> getEventsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get events with status: {}", status);
        Page<EventDto> result = eventService.getEventsByStatus(status, pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to search events with query: {}", q);
        Page<EventDto> result = eventService.searchEvents(q, pageable);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto>> getUpcomingEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("REST request to get upcoming events from: {}", from);
        List<EventDto> result = eventService.getUpcomingEvents(from, limit);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/calendar")
    public ResponseEntity<List<EventDto>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("REST request to get calendar events between {} and {}", start, end);
        List<EventDto> result = eventService.getEventsInDateRange(start, end);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{eventId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDto> publishEvent(
            @PathVariable Long eventId,
            @CurrentUser Long userId) throws EventException, UnauthorizedException {
        log.info("REST request to publish event ID: {}", eventId);
        EventDto result = eventService.publishEvent(eventId, userId);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{eventId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventDto> cancelEvent(
            @PathVariable Long eventId,
            @RequestParam String reason,
            @CurrentUser Long userId) throws EventException, UnauthorizedException {
        log.info("REST request to cancel event ID: {}", eventId);
        EventDto result = eventService.cancelEvent(eventId, userId, reason);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{eventId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventStatistics> getEventStatistics(
            @PathVariable Long eventId) throws EventException {
        log.info("REST request to get statistics for event ID: {}", eventId);
        EventStatistics result = eventService.getEventStatistics(eventId);
        return ResponseEntity.ok(result);
    }
}