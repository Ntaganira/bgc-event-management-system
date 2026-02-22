package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : AttendanceRepository.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for Attendance entity operations
 * </pre>
 */

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByRegistration(Registration registration);
    
    Optional<Attendance> findByRegistrationId(Long registrationId);
    
    List<Attendance> findByEvent(Event event);
    
    Page<Attendance> findByEventId(Long eventId, Pageable pageable);
    
    @Query("SELECT a FROM Attendance a WHERE a.event.id = :eventId AND a.checkedInAt BETWEEN :start AND :end")
    List<Attendance> findByEventAndDateRange(
            @Param("eventId") Long eventId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT DATE(a.checkedInAt) as date, COUNT(a) as count " +
           "FROM Attendance a " +
           "WHERE a.event.id = :eventId " +
           "GROUP BY DATE(a.checkedInAt) " +
           "ORDER BY date")
    List<Object[]> getAttendanceTrend(@Param("eventId") Long eventId);
    
    @Query("SELECT a.checkInMethod, COUNT(a) FROM Attendance a " +
           "WHERE a.event.id = :eventId " +
           "GROUP BY a.checkInMethod")
    List<Object[]> getCheckInMethods(@Param("eventId") Long eventId);
    
    @Query("SELECT a FROM Attendance a WHERE a.checkedInBy = :userId ORDER BY a.checkedInAt DESC")
    List<Attendance> findByCheckedInBy(@Param("userId") Long userId, Pageable pageable);
    
    boolean existsByRegistrationId(Long registrationId);
}