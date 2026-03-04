package com.bgc.event.service;

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.EventDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Event create(EventDto dto, User creator);

    Event update(Long id, EventDto dto);

    void delete(Long id);

    Optional<Event> findById(Long id);

    List<Event> findAll();

    List<Event> findUpcoming();

    List<CalendarEventDto> findAllAsCalendarEvents();

    long count();
        
    /**
     * Find event by ID with attendance records loaded (for attendance marking)
     */
    Optional<Event> findByIdWithAttendance(Long id);
    
    /**
     * Find event by ID with all relations loaded (for detailed view)
     */
    Optional<Event> findByIdWithAll(Long id);
    
    /**
     * Find all events with attendance records loaded (for dashboard)
     */
    List<Event> findAllWithAttendance();
    
    /**
     * Find upcoming events with attendance records loaded
     */
    List<Event> findUpcomingWithAttendance();
    
    /**
     * Get attendance count for an event without loading the collection
     */
    int getAttendanceCount(Long eventId);
    
    /**
     * Get total attendees count across all events (for dashboard stats)
     */
    long getTotalAttendanceCount();

    Page<Event> findPaginated(String search, Pageable pageable);
}