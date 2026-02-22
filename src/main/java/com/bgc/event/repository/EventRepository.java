package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : EventRepository.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for Event entity operations
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.status IN :statuses AND e.startDate >= :fromDate")
    List<Event> findActiveEvents(@Param("statuses") List<Event.EventStatus> statuses,
                                 @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Event> searchEvents(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :start AND :end")
    List<Event> findEventsInDateRange(@Param("start") LocalDateTime start, 
                                      @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer.id = :organizerId")
    long countEventsByOrganizer(@Param("organizerId") Long organizerId);
    
    @Query("SELECT SUM(e.currentRegistrations) FROM Event e WHERE e.organizer.id = :organizerId")
    Long getTotalRegistrationsByOrganizer(@Param("organizerId") Long organizerId);
    
    @Query("SELECT e FROM Event e WHERE e.capacity - e.currentRegistrations < :threshold")
    List<Event> findEventsWithLowCapacity(@Param("threshold") int threshold);
    
    @Modifying
    @Query("UPDATE Event e SET e.currentRegistrations = e.currentRegistrations + 1 WHERE e.id = :eventId")
    int incrementRegistrationCount(@Param("eventId") Long eventId);
    
    @Modifying
    @Query("UPDATE Event e SET e.status = :status WHERE e.id = :eventId")
    int updateEventStatus(@Param("eventId") Long eventId, @Param("status") Event.EventStatus status);
}