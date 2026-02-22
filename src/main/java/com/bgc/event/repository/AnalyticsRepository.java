package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : AnalyticsRepository.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom repository for analytics queries
 * </pre>
 */

import com.bgc.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AnalyticsRepository extends JpaRepository<Event, Long> {
    
    // Dashboard overview queries
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.deleted = false")
    Long getTotalEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'OPEN' AND e.deleted = false")
    Long getActiveEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.startDate > CURRENT_TIMESTAMP AND e.deleted = false")
    Long getUpcomingEvents();
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'COMPLETED' AND e.deleted = false")
    Long getCompletedEvents();
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.deleted = false")
    Long getTotalRegistrations();
    
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.checkedIn = true AND r.deleted = false")
    Long getTotalAttendees();
    
    // Registration trends
    
    @Query(value = "SELECT DATE(r.created_at) as date, COUNT(r) as count " +
           "FROM registrations r " +
           "WHERE r.created_at >= :startDate " +
           "GROUP BY DATE(r.created_at) " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> getRegistrationTrend(@Param("startDate") LocalDateTime startDate);
    
    // Event statistics
    
    @Query("SELECT new map(e.id as eventId, e.title as title, " +
           "COUNT(r) as registrations, " +
           "SUM(CASE WHEN r.checkedIn = true THEN 1 ELSE 0 END) as checkedIn) " +
           "FROM Event e LEFT JOIN e.registrations r " +
           "WHERE e.deleted = false " +
           "GROUP BY e.id, e.title " +
           "ORDER BY registrations DESC")
    List<Map<String, Object>> getTopEventsByRegistrations();
    
    @Query("SELECT new map(e.id as eventId, e.title as title, " +
           "COUNT(r) as registrations, " +
           "SUM(CASE WHEN r.checkedIn = true THEN 1 ELSE 0 END) as checkedIn) " +
           "FROM Event e LEFT JOIN e.registrations r " +
           "WHERE e.deleted = false " +
           "GROUP BY e.id, e.title " +
           "ORDER BY checkedIn DESC")
    List<Map<String, Object>> getTopEventsByAttendance();
    
    // Event-specific analytics
    
    @Query("SELECT COUNT(r) FROM Registration r " +
           "WHERE r.event.id = :eventId AND r.status = :status")
    Long countRegistrationsByStatus(@Param("eventId") Long eventId, 
                                    @Param("status") String status);
    
    @Query("SELECT r.organization, COUNT(r) FROM Registration r " +
           "WHERE r.event.id = :eventId AND r.organization IS NOT NULL " +
           "GROUP BY r.organization ORDER BY COUNT(r) DESC")
    List<Object[]> getRegistrationsByOrganization(@Param("eventId") Long eventId);
    
    @Query("SELECT r.jobTitle, COUNT(r) FROM Registration r " +
           "WHERE r.event.id = :eventId AND r.jobTitle IS NOT NULL " +
           "GROUP BY r.jobTitle ORDER BY COUNT(r) DESC")
    List<Object[]> getRegistrationsByJobTitle(@Param("eventId") Long eventId);
    
    @Query(value = "SELECT EXTRACT(HOUR FROM created_at) as hour, COUNT(*) as count " +
           "FROM registrations WHERE event_id = :eventId " +
           "GROUP BY hour ORDER BY hour", nativeQuery = true)
    List<Object[]> getRegistrationHourDistribution(@Param("eventId") Long eventId);
    
    // Time-based analytics
    
    @Query("SELECT FUNCTION('DATE', r.createdAt) as date, COUNT(r) " +
           "FROM Registration r " +
           "WHERE r.event.id = :eventId " +
           "GROUP BY FUNCTION('DATE', r.createdAt) " +
           "ORDER BY date")
    List<Object[]> getDailyRegistrations(@Param("eventId") Long eventId);
    
    @Query("SELECT FUNCTION('DATE', r.checkedInAt) as date, COUNT(r) " +
           "FROM Registration r " +
           "WHERE r.event.id = :eventId AND r.checkedIn = true " +
           "GROUP BY FUNCTION('DATE', r.checkedInAt) " +
           "ORDER BY date")
    List<Object[]> getDailyCheckIns(@Param("eventId") Long eventId);
    
    // Organizer analytics
    
    @Query("SELECT u.id, u.firstName, u.lastName, " +
           "COUNT(e) as eventCount, " +
           "SUM(e.currentRegistrations) as totalRegistrations, " +
           "AVG(e.currentRegistrations) as avgRegistrations " +
           "FROM User u LEFT JOIN u.organizedEvents e " +
           "WHERE u.deleted = false " +
           "GROUP BY u.id, u.firstName, u.lastName")
    List<Object[]> getOrganizerPerformance();
    
    // Comparison queries
    
    @Query("SELECT AVG(CAST(r.checkedIn AS double) / NULLIF(e.currentRegistrations, 0)) " +
           "FROM Event e JOIN e.registrations r " +
           "WHERE e.id != :eventId AND e.status = 'COMPLETED'")
    Double getAverageAttendanceRateForSimilarEvents(@Param("eventId") Long eventId);
}