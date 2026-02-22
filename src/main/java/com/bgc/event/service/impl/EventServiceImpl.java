package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : EventServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Event Management Service
 * </pre>
 */

import com.bgc.event.dto.EventCreateRequest;
import com.bgc.event.dto.EventDto;
import com.bgc.event.dto.EventStatistics;
import com.bgc.event.dto.EventUpdateRequest;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.UnauthorizedException;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AuditService;
import com.bgc.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final AuditService auditService;
    
    @Override
    @Caching(evict = {
        @CacheEvict(value = "events", allEntries = true),
        @CacheEvict(value = "calendar-events", allEntries = true)
    })
    public EventDto createEvent(EventCreateRequest request, Long organizerId) throws EventException {
        log.info("Creating new event for organizer ID: {}", organizerId);
        
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> {
                    log.error("Organizer not found with ID: {}", organizerId);
                    return new EventException("Organizer not found");
                });
        
        // Validate dates
        validateEventDates(request.getStartDate(), request.getEndDate(), request.getRegistrationDeadline());
        
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .venue(request.getVenue())
                .address(request.getAddress())
                .city(request.getCity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .capacity(request.getCapacity())
                .currentRegistrations(0)
                .waitlistCapacity(request.getWaitlistCapacity())
                .currentWaitlist(0)
                .status(Event.EventStatus.DRAFT)
                .featuredImage(request.getFeaturedImage())
                .colorCode(request.getColorCode() != null ? request.getColorCode() : "#3788d8")
                .termsAndConditions(request.getTermsAndConditions())
                .allowWaitlist(request.isAllowWaitlist())
                .requireApproval(request.isRequireApproval())
                .tags(request.getTags())
                .organizer(organizer)
                .build();
        
        Event savedEvent = eventRepository.save(event);
        
        auditService.logAction("CREATE_EVENT", organizerId, "Event", savedEvent.getId(), 
                "Created event: " + savedEvent.getTitle());
        
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return mapToDto(savedEvent);
    }
    
    @Override
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#eventId"),
        @CacheEvict(value = "calendar-events", allEntries = true)
    })
    public EventDto updateEvent(Long eventId, EventUpdateRequest request, Long userId) 
            throws EventException, UnauthorizedException {
        
        log.info("Updating event ID: {} by user ID: {}", eventId, userId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventException("Event not found");
                });
        
        // Check permission
        if (!canManageEvent(eventId, userId)) {
            log.warn("User ID: {} attempted to update event ID: {} without permission", userId, eventId);
            throw new UnauthorizedException("You don't have permission to update this event");
        }
        
        // Don't allow updates to published events without proper validation
        if (event.getStatus() == Event.EventStatus.PUBLISHED || 
            event.getStatus() == Event.EventStatus.OPEN) {
            validatePublishedEventUpdate(event, request);
        }
        
        // Update fields if provided
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getShortDescription() != null) event.setShortDescription(request.getShortDescription());
        if (request.getVenue() != null) event.setVenue(request.getVenue());
        if (request.getAddress() != null) event.setAddress(request.getAddress());
        if (request.getCity() != null) event.setCity(request.getCity());
        
        // Validate and update dates
        if (request.getStartDate() != null || request.getEndDate() != null || 
            request.getRegistrationDeadline() != null) {
            
            LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : event.getStartDate();
            LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : event.getEndDate();
            LocalDateTime deadline = request.getRegistrationDeadline() != null ? 
                    request.getRegistrationDeadline() : event.getRegistrationDeadline();
            
            validateEventDates(startDate, endDate, deadline);
            
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setRegistrationDeadline(deadline);
        }
        
        if (request.getCapacity() != null) {
            if (request.getCapacity() < event.getCurrentRegistrations()) {
                throw new EventException("Cannot reduce capacity below current registrations");
            }
            event.setCapacity(request.getCapacity());
        }
        
        if (request.getWaitlistCapacity() != null) {
            event.setWaitlistCapacity(request.getWaitlistCapacity());
        }
        
        if (request.getFeaturedImage() != null) event.setFeaturedImage(request.getFeaturedImage());
        if (request.getColorCode() != null) event.setColorCode(request.getColorCode());
        if (request.getTermsAndConditions() != null) event.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getTags() != null) event.setTags(request.getTags());
        
        event.setAllowWaitlist(request.isAllowWaitlist());
        event.setRequireApproval(request.isRequireApproval());
        
        // Update status based on new information
        updateEventStatus(eventId);
        
        Event updatedEvent = eventRepository.save(event);
        
        auditService.logAction("UPDATE_EVENT", userId, "Event", eventId, 
                "Updated event: " + event.getTitle());
        
        log.info("Event ID: {} updated successfully", eventId);
        return mapToDto(updatedEvent);
    }
    
    @Override
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#eventId"),
        @CacheEvict(value = "calendar-events", allEntries = true)
    })
    public void deleteEvent(Long eventId, Long userId) throws EventException, UnauthorizedException {
        log.info("Deleting event ID: {} by user ID: {}", eventId, userId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventException("Event not found");
                });
        
        // Check permission
        if (!canManageEvent(eventId, userId)) {
            log.warn("User ID: {} attempted to delete event ID: {} without permission", userId, eventId);
            throw new UnauthorizedException("You don't have permission to delete this event");
        }
        
        // Soft delete
        event.setDeleted(true);
        event.setDeletedAt(LocalDateTime.now());
        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
        
        auditService.logAction("DELETE_EVENT", userId, "Event", eventId, 
                "Deleted event: " + event.getTitle());
        
        log.info("Event ID: {} deleted successfully", eventId);
    }
    
    @Override
    @Cacheable(value = "events", key = "#eventId")
    @Transactional(readOnly = true)
    public EventDto getEventById(Long eventId) throws EventException {
        log.debug("Fetching event by ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventException("Event not found");
                });
        
        return mapToDto(event);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getAllEvents(Pageable pageable) {
        log.debug("Fetching all events with page: {}", pageable);
        
        Page<Event> events = eventRepository.findByDeletedFalse(pageable);
        return events.map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getEventsByOrganizer(Long organizerId, Pageable pageable) {
        log.debug("Fetching events for organizer ID: {}", organizerId);
        
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new EventException("Organizer not found"));
        
        Page<Event> events = eventRepository.findByOrganizer(organizer, pageable);
        return events.map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> getEventsByStatus(String status, Pageable pageable) {
        log.debug("Fetching events with status: {}", status);
        
        try {
            Event.EventStatus eventStatus = Event.EventStatus.valueOf(status.toUpperCase());
            Page<Event> events = eventRepository.findByStatus(eventStatus, pageable);
            return events.map(this::mapToDto);
        } catch (IllegalArgumentException e) {
            log.error("Invalid event status: {}", status);
            return Page.empty();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EventDto> searchEvents(String searchTerm, Pageable pageable) {
        log.debug("Searching events with term: {}", searchTerm);
        
        Page<Event> events = eventRepository.searchEvents(searchTerm, pageable);
        return events.map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEvents(LocalDateTime fromDate, int limit) {
        log.debug("Fetching upcoming events from: {}", fromDate);
        
        List<Event.EventStatus> activeStatuses = Arrays.asList(
                Event.EventStatus.OPEN, Event.EventStatus.PUBLISHED
        );
        
        List<Event> events = eventRepository.findActiveEvents(activeStatuses, fromDate)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return events.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "calendar-events", key = "#start.toString() + #end.toString()")
    @Transactional(readOnly = true)
    public List<EventDto> getEventsInDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching events between {} and {}", start, end);
        
        List<Event> events = eventRepository.findEventsInDateRange(start, end);
        return events.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    @Override
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#eventId"),
        @CacheEvict(value = "calendar-events", allEntries = true)
    })
    public EventDto publishEvent(Long eventId, Long userId) throws EventException, UnauthorizedException {
        log.info("Publishing event ID: {} by user ID: {}", eventId, userId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        
        if (!canManageEvent(eventId, userId)) {
            throw new UnauthorizedException("You don't have permission to publish this event");
        }
        
        if (event.getStatus() != Event.EventStatus.DRAFT) {
            throw new EventException("Event can only be published from DRAFT status");
        }
        
        // Validate event is ready for publication
        if (event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new EventException("Cannot publish event with past start date");
        }
        
        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            throw new EventException("Event must have a valid capacity before publishing");
        }
        
        event.setStatus(Event.EventStatus.OPEN);
        event.setPublishedAt(LocalDateTime.now());
        
        Event publishedEvent = eventRepository.save(event);
        
        auditService.logAction("PUBLISH_EVENT", userId, "Event", eventId, 
                "Published event: " + event.getTitle());
        
        log.info("Event ID: {} published successfully", eventId);
        return mapToDto(publishedEvent);
    }
    
    @Override
    @Caching(evict = {
        @CacheEvict(value = "events", key = "#eventId"),
        @CacheEvict(value = "calendar-events", allEntries = true)
    })
    public EventDto cancelEvent(Long eventId, Long userId, String reason) 
            throws EventException, UnauthorizedException {
        
        log.info("Cancelling event ID: {} by user ID: {}", eventId, userId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        
        if (!canManageEvent(eventId, userId)) {
            throw new UnauthorizedException("You don't have permission to cancel this event");
        }
        
        event.setStatus(Event.EventStatus.CANCELLED);
        
        // Here you would also notify all registered attendees
        // This would be handled by an async notification service
        
        Event cancelledEvent = eventRepository.save(event);
        
        auditService.logAction("CANCEL_EVENT", userId, "Event", eventId, 
                "Cancelled event: " + event.getTitle() + ". Reason: " + reason);
        
        log.info("Event ID: {} cancelled successfully", eventId);
        return mapToDto(cancelledEvent);
    }
    
    @Override
    @Transactional
    public void updateEventStatus(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return;
        
        LocalDateTime now = LocalDateTime.now();
        Event.EventStatus oldStatus = event.getStatus();
        Event.EventStatus newStatus = oldStatus;
        
        // Don't update cancelled or completed events
        if (oldStatus == Event.EventStatus.CANCELLED || oldStatus == Event.EventStatus.COMPLETED) {
            return;
        }
        
        // Check if event is completed
        if (event.getEndDate().isBefore(now)) {
            newStatus = Event.EventStatus.COMPLETED;
        }
        // Check if event is full
        else if (event.getCapacity() != null && event.getCurrentRegistrations() >= event.getCapacity()) {
            newStatus = Event.EventStatus.FULL;
        }
        // Check if registration deadline passed
        else if (event.getRegistrationDeadline() != null && event.getRegistrationDeadline().isBefore(now)) {
            newStatus = Event.EventStatus.CLOSED;
        }
        // Check if event is open for registration
        else if (event.getStatus() == Event.EventStatus.OPEN || 
                 event.getStatus() == Event.EventStatus.PUBLISHED) {
            newStatus = Event.EventStatus.OPEN;
        }
        
        if (oldStatus != newStatus) {
            event.setStatus(newStatus);
            eventRepository.save(event);
            log.info("Event ID: {} status updated from {} to {}", eventId, oldStatus, newStatus);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public EventStatistics getEventStatistics(Long eventId) throws EventException {
       EventDto event = getEventById(eventId);
        
        long totalRegistrations = registrationRepository.countByEventId(eventId);
        long checkedIn = registrationRepository.countCheckedInByEvent(eventId);
        int waitlistCount = registrationRepository.countWaitlistByEvent(eventId);
        
        List<Object[]> trend = registrationRepository.getRegistrationTrend(eventId);
        
        return EventStatistics.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .totalRegistrations((int) totalRegistrations)
                .checkedIn((int) checkedIn)
                .attendanceRate(event.getCurrentRegistrations() > 0 ? 
                        (double) checkedIn / event.getCurrentRegistrations() * 100 : 0)
                .waitlistCount(waitlistCount)
                .availableSpots(event.getAvailableSpots())
                .isFull(event.getIsFull())
                .registrationTrend(trend)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canManageEvent(Long eventId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        
        // Admin can manage any event
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            return true;
        }
        
        // Organizer can only manage their own events
        Event event = eventRepository.findById(eventId).orElse(null);
        return event != null && event.getOrganizer().getId().equals(userId);
    }
    
    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate, LocalDateTime deadline) {
        if (endDate.isBefore(startDate)) {
            throw new EventException("End date must be after start date");
        }
        
        if (deadline != null && deadline.isAfter(startDate)) {
            throw new EventException("Registration deadline must be before event start date");
        }
        
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new EventException("Registration deadline cannot be in the past");
        }
    }
    
    private void validatePublishedEventUpdate(Event event, EventUpdateRequest request) {
        // For published events, prevent certain changes
        if (request.getStartDate() != null && !request.getStartDate().equals(event.getStartDate())) {
            throw new EventException("Cannot change start date of a published event");
        }
        
        if (request.getCapacity() != null && request.getCapacity() < event.getCurrentRegistrations()) {
            throw new EventException("Cannot reduce capacity below current registrations");
        }
    }
    
    private EventDto mapToDto(Event event) {
        EventDto dto = EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .shortDescription(event.getShortDescription())
                .venue(event.getVenue())
                .address(event.getAddress())
                .city(event.getCity())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .capacity(event.getCapacity())
                .currentRegistrations(event.getCurrentRegistrations())
                .waitlistCapacity(event.getWaitlistCapacity())
                .currentWaitlist(event.getCurrentWaitlist())
                .status(event.getStatus().toString())
                .featuredImage(event.getFeaturedImage())
                .colorCode(event.getColorCode())
                .termsAndConditions(event.getTermsAndConditions())
                .allowWaitlist(event.isAllowWaitlist())
                .requireApproval(event.isRequireApproval())
                .tags(event.getTags())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .isPublished(event.getPublishedAt() != null)
                .build();
        
        // Set derived fields
        dto.setFull(dto.getIsFull());
        dto.setRegistrationOpen(dto.getIsRegistrationOpen());
        dto.setAvailableSpots(dto.getAvailableSpots());
        
        return dto;
    }
}