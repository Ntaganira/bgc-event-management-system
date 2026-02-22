package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : CalendarService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for calendar operations
 * </pre>
 */

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.CalendarViewRequest;
import com.bgc.event.dto.TimeSlotDto;
import com.bgc.event.exception.EventException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CalendarService {
    
    /**
     * Get events for calendar display
     * @param request Calendar view parameters
     * @return List of calendar events
     */
    List<CalendarEventDto> getCalendarEvents(CalendarViewRequest request);
    
    /**
     * Get event details for calendar popup
     * @param eventId Event ID
     * @return Calendar event details
     */
    CalendarEventDto getCalendarEventDetails(Long eventId) throws EventException;
    
    /**
     * Get color mapping for event statuses
     * @return Map of status to color
     */
    Map<String, String> getStatusColorMapping();
    
    /**
     * Update event from calendar drag/drop
     * @param eventId Event ID
     * @param newStart New start date/time
     * @param newEnd New end date/time
     * @param userId User ID
     * @return Updated calendar event
     */
    CalendarEventDto updateEventFromCalendar(Long eventId, LocalDateTime newStart, 
                                            LocalDateTime newEnd, Long userId) 
            throws EventException;
    
    /**
     * Get available time slots for an event
     * @param eventId Event ID
     * @return List of available time slots
     */
    List<TimeSlotDto> getAvailableTimeSlots(Long eventId);
    
    /**
     * Export calendar as iCal
     * @param request Calendar view parameters
     * @return iCal file content
     */
    String exportToICal(CalendarViewRequest request);
}