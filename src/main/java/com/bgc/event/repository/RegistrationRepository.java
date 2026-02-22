package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : RegistrationRepository.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for Registration entity operations
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

       @Cacheable(value = "registrations", key = "#qrCode")
       Optional<Registration> findByQrCode(String qrCode);

       Optional<Registration> findByRegistrationToken(String token);

       boolean existsByEventIdAndEmail(Long eventId, String email);

       Page<Registration> findByEvent(Event event, Pageable pageable);

       @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId")
       Page<Registration> findByEventId(@Param("eventId") Long eventId, Pageable pageable);

       @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.checkedIn = true")
       List<Registration> findCheckedInAttendees(@Param("eventId") Long eventId);

       @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.checkedIn = true")
       long countCheckedInByEvent(@Param("eventId") Long eventId);

       @Query("SELECT r FROM Registration r WHERE r.email = :email ORDER BY r.createdAt DESC")
       List<Registration> findRegistrationsByEmail(@Param("email") String email);

       @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = :status")
       List<Registration> findByEventAndStatus(@Param("eventId") Long eventId,
                     @Param("status") Registration.RegistrationStatus status);

       @Query("SELECT DATE(r.createdAt) as date, COUNT(r) as count " +
                     "FROM Registration r " +
                     "WHERE r.event.id = :eventId " +
                     "GROUP BY DATE(r.createdAt) " +
                     "ORDER BY date")
       List<Object[]> getRegistrationTrend(@Param("eventId") Long eventId);

       @Modifying
       @Query("UPDATE Registration r SET r.checkedIn = true, r.checkedInAt = :now, r.status = 'ATTENDED' " +
                     "WHERE r.id = :registrationId")
       int markAsCheckedIn(@Param("registrationId") Long registrationId, @Param("now") LocalDateTime now);

       @Modifying
       @Query("UPDATE Registration r SET r.status = 'CANCELLED', r.cancelledAt = :now " +
                     "WHERE r.id = :registrationId")
       int cancelRegistration(@Param("registrationId") Long registrationId, @Param("now") LocalDateTime now);

       @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'WAITLISTED'")
       int countWaitlistByEvent(@Param("eventId") Long eventId);

       @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId")
       long countByEventId(@Param("eventId") Long eventId);

       @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = :status")
       long countByEventAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

       @Query("SELECT r FROM Registration r WHERE r.event.id = :eventId AND r.status = :status ORDER BY r.createdAt ASC")
       List<Registration> findByEventAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

       @Query("SELECT r FROM Registration r WHERE r.id = :id AND r.email = :email")
       Optional<Registration> findByIdAndEmail(@Param("id") Long id, @Param("email") String email);

       // Add these methods to RegistrationRepository.java

       /**
        * Get monthly registration stats for chart
        */
       @Query("SELECT FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM') as month, " +
                     "SUM(CASE WHEN r.status IN ('CONFIRMED', 'ATTENDED') THEN 1 ELSE 0 END) as approved, " +
                     "SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) as pending " +
                     "FROM Registration r " +
                     "WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.deleted = false " +
                     "GROUP BY FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM') " +
                     "ORDER BY month")
       List<Object[]> getMonthlyRegistrationStats(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Get registration status distribution
        */
       @Query("SELECT r.status, COUNT(r) FROM Registration r WHERE r.deleted = false GROUP BY r.status")
       List<Object[]> getRegistrationStatusDistribution();

       /**
        * Get daily registration trend for all events
        */
       @Query("SELECT FUNCTION('DATE', r.createdAt), COUNT(r) FROM Registration r " +
                     "WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.deleted = false " +
                     "GROUP BY FUNCTION('DATE', r.createdAt) ORDER BY FUNCTION('DATE', r.createdAt)")
       List<Object[]> getDailyRegistrationTrend(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Get daily registration trend for specific event
        */
       @Query("SELECT FUNCTION('DATE', r.createdAt), COUNT(r) FROM Registration r " +
                     "WHERE r.event.id = :eventId AND r.createdAt BETWEEN :startDate AND :endDate AND r.deleted = false "
                     +
                     "GROUP BY FUNCTION('DATE', r.createdAt) ORDER BY FUNCTION('DATE', r.createdAt)")
       List<Object[]> getDailyRegistrationTrendForEvent(@Param("eventId") Long eventId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Get hourly distribution for all events
        */
       @Query("SELECT EXTRACT(HOUR FROM r.createdAt), COUNT(r) FROM Registration r " +
                     "WHERE r.deleted = false GROUP BY EXTRACT(HOUR FROM r.createdAt) ORDER BY EXTRACT(HOUR FROM r.createdAt)")
       List<Object[]> getHourlyDistribution();

       /**
        * Get hourly distribution for specific event
        */
       @Query("SELECT EXTRACT(HOUR FROM r.createdAt), COUNT(r) FROM Registration r " +
                     "WHERE r.event.id = :eventId AND r.deleted = false " +
                     "GROUP BY EXTRACT(HOUR FROM r.createdAt) ORDER BY EXTRACT(HOUR FROM r.createdAt)")
       List<Object[]> getHourlyDistributionForEvent(@Param("eventId") Long eventId);

       /**
        * Count registrations by date range
        */
       long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

       /**
        * Count registrations after date
        */
       long countByCreatedAtAfter(LocalDateTime date);

       /**
        * Count registrations for event after date
        */
       long countByEventIdAndCreatedAtAfter(Long eventId, LocalDateTime date);

       /**
        * Get top organizations
        */
       @Query("SELECT r.organization, COUNT(r) FROM Registration r " +
                     "WHERE r.organization IS NOT NULL AND r.organization != '' AND r.deleted = false " +
                     "GROUP BY r.organization ORDER BY COUNT(r) DESC")
       List<Object[]> getTopOrganizations(Pageable pageable);

       /**
        * Count by status for event
        */
       @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = :status AND r.deleted = false")
       long countByEventAndStatus(@Param("eventId") Long eventId,
                     @Param("status") Registration.RegistrationStatus status);

       /**
        * Count by event ID (deleted false)
        */
       long countByEventIdAndDeletedFalse(Long eventId);
}