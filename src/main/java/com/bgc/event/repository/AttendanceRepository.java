package com.bgc.event.repository;

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndEvent(User user, Event event);
    boolean existsByUserAndEvent(User user, Event event);
    List<Attendance> findByEvent(Event event);
    List<Attendance> findByUser(User user);
    long countByEvent(Event event);
}
