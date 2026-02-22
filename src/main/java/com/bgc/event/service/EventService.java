package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : EventService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for event management operations
 * </pre>
 */

import com.bgc.event.dto.EventCreateRequest;
import com.bgc.event.dto.EventDto;
import com.bgc.event.dto.EventStatistics;
import com.bgc.event.dto.EventUpdateRequest;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    
    /**
     * Create a new event
     * @param request Event creation request
     * @param organizerId ID of the organizer creating the event
     * @return Created event DTO
     * @throws EventException if validation fails
     */
    EventDto createEvent(EventCreateRequest request, Long organizerId) throws EventException;
    
    /**
     * Update an existing event
     * @param eventId ID of the event to update
     * @param request Update request
     * @param userId ID of the user making the request
     * @return Updated event DTO
     * @throws EventException if event not found or validation fails
     * @throws UnauthorizedException if user doesn't have permission
     */
    EventDto updateEvent(Long eventId, EventUpdateRequest request, Long userId) 
            throws EventException, UnauthorizedException;
    
    /**
     * Delete an event (soft delete)
     * @param eventId ID of the event to delete
     * @param userId ID of the user making the request
     * @throws EventException if event not found
     * @throws UnauthorizedException if user doesn't have permission
     */
    void deleteEvent(Long eventId, Long userId) throws EventException, UnauthorizedException;
    
    /**
     * Get event by ID
     * @param eventId Event ID
     * @return Event DTO
     * @throws EventException if event not found
     */
    EventDto getEventById(Long eventId) throws EventException;
    
    /**
     * Get all events with pagination
     * @param pageable Pagination information
     * @return Page of event DTOs
     */
    Page<EventDto> getAllEvents(Pageable pageable);
    
    /**
     * Get events by organizer
     * @param organizerId Organizer ID
     * @param pageable Pagination information
     * @return Page of event DTOs
     */
    Page<EventDto> getEventsByOrganizer(Long organizerId, Pageable pageable);
    
    /**
     * Get events by status
     * @param status Event status
     * @param pageable Pagination information
     * @return Page of event DTOs
     */
    Page<EventDto> getEventsByStatus(String status, Pageable pageable);
    
    /**
     * Search events
     * @param searchTerm Search term
     * @param pageable Pagination information
     * @return Page of event DTOs
     */
    Page<EventDto> searchEvents(String searchTerm, Pageable pageable);
    
    /**
     * Get upcoming events
     * @param fromDate Start date
     * @param limit Maximum number of events
     * @return List of event DTOs
     */
    List<EventDto> getUpcomingEvents(LocalDateTime fromDate, int limit);
    
    /**
     * Get events in date range for calendar
     * @param start Start date
     * @param end End date
     * @return List of event DTOs for calendar
     */
    List<EventDto> getEventsInDateRange(LocalDateTime start, LocalDateTime end);
    
    /**
     * Publish an event
     * @param eventId Event ID
     * @param userId User ID
     * @return Updated event DTO
     * @throws EventException if event cannot be published
     */
    EventDto publishEvent(Long eventId, Long userId) throws EventException, UnauthorizedException;
    
    /**
     * Cancel an event
     * @param eventId Event ID
     * @param userId User ID
     * @param reason Cancellation reason
     * @return Updated event DTO
     * @throws EventException if event cannot be cancelled
     */
    EventDto cancelEvent(Long eventId, Long userId, String reason) throws EventException, UnauthorizedException;
    
    /**
     * Update event status based on capacity and deadlines
     * @param eventId Event ID
     */
    void updateEventStatus(Long eventId);
    
    /**
     * Get event statistics
     * @param eventId Event ID
     * @return Event statistics
     */
    EventStatistics getEventStatistics(Long eventId) throws EventException;
    
    /**
     * Check if user can manage event
     * @param eventId Event ID
     * @param userId User ID
     * @return true if user can manage
     */
    boolean canManageEvent(Long eventId, Long userId);
}