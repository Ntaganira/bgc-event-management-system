package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : CalendarServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Calendar Service with FullCalendar integration
 * </pre>
 */

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.CalendarViewRequest;
import com.bgc.event.dto.TimeSlotDto;
import com.bgc.event.entity.Event;
import com.bgc.event.exception.EventException;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.service.CalendarService;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
    
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserService userService;
    
    private static final Map<String, String> STATUS_COLORS = Map.of(
        "DRAFT", "#6c757d",      // Gray
        "OPEN", "#28a745",       // Green
        "FULL", "#dc3545",       // Red
        "CLOSED", "#ffc107",      // Yellow
        "CANCELLED", "#343a40",   // Dark Gray
        "COMPLETED", "#17a2b8",   // Teal
        "PUBLISHED", "#007bff"    // Blue
    );
    
    @Override
    @Cacheable(value = "calendar-events", key = "#request.start + #request.end + #request.organizerId + #request.status")
    public List<CalendarEventDto> getCalendarEvents(CalendarViewRequest request) {
        log.info("Fetching calendar events from {} to {}", request.getStart(), request.getEnd());
        
        // Default to current month if dates not provided
        LocalDateTime start = request.getStart() != null ? request.getStart() : 
                              LocalDateTime.now().minusDays(15);
        LocalDateTime end = request.getEnd() != null ? request.getEnd() : 
                            LocalDateTime.now().plusMonths(2);
        
        // Get events in date range
        List<Event> events = eventRepository.findEventsInDateRange(start, end);
        
        // Apply filters
        if (request.getOrganizerId() != null) {
            events = events.stream()
                    .filter(e -> e.getOrganizer().getId().equals(request.getOrganizerId()))
                    .collect(Collectors.toList());
        }
        
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getStatus().toString().equalsIgnoreCase(request.getStatus()))
                    .collect(Collectors.toList());
        }
        
        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            String searchLower = request.getSearch().toLowerCase();
            events = events.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(searchLower) ||
                                (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchLower)) ||
                                e.getVenue().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }
        
        // Convert to calendar DTOs
        return events.stream()
                .map(event -> mapToCalendarDto(event, request.getPublicView()))
                .collect(Collectors.toList());
    }
    
    @Override
    public CalendarEventDto getCalendarEventDetails(Long eventId) throws EventException {
        log.debug("Fetching calendar details for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found with ID: " + eventId));
        
        return mapToDetailedCalendarDto(event);
    }
    
    @Override
    public Map<String, String> getStatusColorMapping() {
        return STATUS_COLORS;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "calendar-events", allEntries = true)
    public CalendarEventDto updateEventFromCalendar(Long eventId, LocalDateTime newStart, 
                                                   LocalDateTime newEnd, Long userId) 
            throws EventException {
        
        log.info("Updating event ID: {} from calendar. New start: {}, new end: {}", 
                eventId, newStart, newEnd);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found with ID: " + eventId));
        
        // Check permission
        if (!canEditEvent(event, userId)) {
            throw new EventException("You don't have permission to edit this event");
        }
        
        // Validate dates
        if (newStart.isBefore(LocalDateTime.now())) {
            throw new EventException("Cannot move event to past date");
        }
        
        if (newEnd != null && newEnd.isBefore(newStart)) {
            throw new EventException("End date must be after start date");
        }
        
        // Check if event has registrations and moving would affect them
        if (event.getCurrentRegistrations() > 0) {
            long conflictingRegistrations = registrationRepository.countByEventId(eventId);
            if (conflictingRegistrations > 0) {
                log.warn("Moving event with {} registrations", conflictingRegistrations);
                // In real implementation, you'd want to notify attendees
            }
        }
        
        // Update dates
        event.setStartDate(newStart);
        if (newEnd != null) {
            event.setEndDate(newEnd);
        }
        
        // Auto-update registration deadline if needed
        if (event.getRegistrationDeadline() != null && 
            event.getRegistrationDeadline().isAfter(newStart)) {
            event.setRegistrationDeadline(newStart.minusDays(1));
        }
        
        Event updatedEvent = eventRepository.save(event);
        
        log.info("Event ID: {} updated from calendar", eventId);
        
        return mapToCalendarDto(updatedEvent, false);
    }
    
    @Override
    public List<TimeSlotDto> getAvailableTimeSlots(Long eventId) {
        log.debug("Getting available time slots for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return Collections.emptyList();
        }
        
        List<TimeSlotDto> slots = new ArrayList<>();
        LocalDateTime start = event.getStartDate();
        LocalDateTime end = event.getEndDate();
        
        // Generate 30-minute slots
        while (start.isBefore(end)) {
            LocalDateTime slotEnd = start.plusMinutes(30);
            
            // Check if slot is available (no conflicting events)
            boolean isAvailable = isTimeSlotAvailable(start, slotEnd, eventId);
            
            slots.add(TimeSlotDto.builder()
                    .start(start)
                    .end(slotEnd)
                    .available(isAvailable)
                    .build());
            
            start = slotEnd;
        }
        
        return slots;
    }
    
    @Override
    public String exportToICal(CalendarViewRequest request) {
        log.info("Exporting calendar to iCal format");
        
        List<CalendarEventDto> events = getCalendarEvents(request);
        
        StringBuilder ical = new StringBuilder();
        ical.append("BEGIN:VCALENDAR\r\n");
        ical.append("VERSION:2.0\r\n");
        ical.append("PRODID:-//BGC Event Management System//EN\r\n");
        ical.append("CALSCALE:GREGORIAN\r\n");
        ical.append("METHOD:PUBLISH\r\n");
        
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        
        for (CalendarEventDto event : events) {
            ical.append("BEGIN:VEVENT\r\n");
            ical.append("UID:").append(event.getId()).append("@bgc.event\r\n");
            ical.append("DTSTART:").append(dateFormat.format(LocalDateTime.parse(event.getStart()))).append("\r\n");
            ical.append("DTEND:").append(dateFormat.format(LocalDateTime.parse(event.getEnd()))).append("\r\n");
            ical.append("SUMMARY:").append(escapeICalText(event.getTitle())).append("\r\n");
            ical.append("DESCRIPTION:").append(escapeICalText(event.getDescription())).append("\r\n");
            ical.append("LOCATION:").append(escapeICalText(event.getVenue())).append("\r\n");
            ical.append("STATUS:").append(mapStatusToICal(event.getStatus())).append("\r\n");
            ical.append("END:VEVENT\r\n");
        }
        
        ical.append("END:VCALENDAR\r\n");
        
        return ical.toString();
    }
    
    private CalendarEventDto mapToCalendarDto(Event event, boolean publicView) {
        CalendarEventDto.CalendarEventDtoBuilder builder = CalendarEventDto.builder()
                .id(String.valueOf(event.getId()))
                .title(event.getTitle())
                .start(event.getStartDate())
                .end(event.getEndDate())
                .allDay(false)
                .color(getStatusColor(event.getStatus().toString()))
                .description(event.getShortDescription())
                .venue(event.getVenue())
                .status(event.getStatus().toString())
                .capacity(event.getCapacity())
                .registrations(event.getCurrentRegistrations())
                .availableSpots(event.getCapacity() != null ? 
                        event.getCapacity() - event.getCurrentRegistrations() : null)
                .organizer(event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName())
                .url("/events/" + event.getId());
        
        // Set edit permissions based on view type
        if (!publicView) {
            builder.editable(true)
                   .startEditable(true)
                   .durationEditable(true);
        }
        
        // Extended properties for custom rendering
        Map<String, Object> extendedProps = new HashMap<>();
        extendedProps.put("capacity", event.getCapacity());
        extendedProps.put("registrations", event.getCurrentRegistrations());
        extendedProps.put("status", event.getStatus());
        extendedProps.put("venue", event.getVenue());
        extendedProps.put("registrationDeadline", event.getRegistrationDeadline());
        builder.extendedProps(extendedProps);
        
        return builder.build();
    }
    
    private CalendarEventDto mapToDetailedCalendarDto(Event event) {
        CalendarEventDto dto = mapToCalendarDto(event, false);
        
        // Add more details for popup
        Map<String, Object> extendedProps = dto.getExtendedProps();
        extendedProps.put("description", event.getDescription());
        extendedProps.put("address", event.getAddress());
        extendedProps.put("city", event.getCity());
        extendedProps.put("tags", event.getTags());
        extendedProps.put("waitlistCount", event.getCurrentWaitlist());
        extendedProps.put("allowWaitlist", event.isAllowWaitlist());
        extendedProps.put("requireApproval", event.isRequireApproval());
        
        return dto;
    }
    
    private String getStatusColor(String status) {
        return STATUS_COLORS.getOrDefault(status.toUpperCase(), "#3788d8");
    }
    
    private boolean canEditEvent(Event event, Long userId) {
        if (userId == null) return false;
        
        return userService.hasRole(userId, "ADMIN") || 
               event.getOrganizer().getId().equals(userId);
    }
    
    private boolean isTimeSlotAvailable(LocalDateTime start, LocalDateTime end, Long excludeEventId) {
        List<Event> conflictingEvents = eventRepository.findEventsInDateRange(start, end);
        return conflictingEvents.stream()
                .filter(e -> !e.getId().equals(excludeEventId))
                .findFirst()
                .isEmpty();
    }
    
    private String escapeICalText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n");
    }
    
    private String mapStatusToICal(String status) {
        switch (status.toUpperCase()) {
            case "CANCELLED":
                return "CANCELLED";
            case "COMPLETED":
                return "COMPLETED";
            default:
                return "CONFIRMED";
        }
    }
}