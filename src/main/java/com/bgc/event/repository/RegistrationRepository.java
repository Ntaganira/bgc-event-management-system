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

       
       long countByEventIdAndDeletedFalse(@Param("eventId") Long eventId);

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
}