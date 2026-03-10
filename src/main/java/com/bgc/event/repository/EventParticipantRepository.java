package com.bgc.event.repository;

import com.bgc.event.entity.Event;
import com.bgc.event.entity.EventParticipant;
import com.bgc.event.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    List<EventParticipant> findByEvent(Event event);
    List<EventParticipant> findByUser(User user);
    List<EventParticipant> findByEventAndRole(Event event, String role);

    Optional<EventParticipant> findByEventAndUserAndRole(Event event, User user, String role);
    boolean existsByEventAndUserAndRole(Event event, User user, String role);

    @Query("SELECT ep FROM EventParticipant ep JOIN FETCH ep.user WHERE ep.event.id = :eventId ORDER BY ep.role")
    List<EventParticipant> findByEventIdWithUser(@Param("eventId") Long eventId);
}
