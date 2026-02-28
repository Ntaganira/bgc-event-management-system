package com.bgc.event.repository;

import com.bgc.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Event> findByStartDateTimeAfterOrderByStartDateTimeAsc(LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :start ORDER BY e.startDateTime ASC")
    List<Event> findUpcomingEvents(LocalDateTime start);
}
