package com.bgc.event.repository;

import com.bgc.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :now ORDER BY e.startDateTime ASC")
    List<Event> findUpcoming(LocalDateTime now);

    /** Events per month for the current year — [monthLabel, count] */
    @Query(value = """
        SELECT MONTH(e.start_date_time) AS m,
               MONTHNAME(e.start_date_time) AS label,
               COUNT(*) AS cnt
        FROM events e
        WHERE YEAR(e.start_date_time) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(e.start_date_time), MONTHNAME(e.start_date_time)
        ORDER BY m ASC
        """, nativeQuery = true)
    List<Object[]> eventsPerMonth();

    /** Upcoming count */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.startDateTime >= :now")
    long countUpcoming(LocalDateTime now);
        
    /**
     * Find all events with attendance records loaded (for dashboard)
     */
    @EntityGraph("Event.withAttendance")
    List<Event> findAll();
    
    /**
     * Find event by ID with attendance records loaded
     */
    @EntityGraph("Event.withAttendance")
    Optional<Event> findById(Long id);
    
    /**
     * Find upcoming events with attendance records loaded
     */
    @EntityGraph("Event.withAttendance")
    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :now ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingWithAttendance(LocalDateTime now);
    
    /**
     * Find events by title with attendance loaded
     */
    @EntityGraph("Event.withAttendance")
    List<Event> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find events by date range with attendance loaded
     */
    @EntityGraph("Event.withAttendance")
    List<Event> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
}