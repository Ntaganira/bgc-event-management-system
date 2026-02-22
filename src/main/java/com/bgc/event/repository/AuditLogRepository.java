package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : AuditLogRepository.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for Audit Log operations
 * </pre>
 */

import com.bgc.event.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);
    
    Page<AuditLog> findByActionCategoryOrderByCreatedAtDesc(
            String actionCategory, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:actionCategory IS NULL OR a.actionCategory = :actionCategory) AND " +
           "(:searchTerm IS NULL OR " +
           "   LOWER(a.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "   LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "   LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "   LOWER(a.changesSummary) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<AuditLog> searchAuditLogs(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userId") Long userId,
            @Param("entityType") String entityType,
            @Param("actionCategory") String actionCategory,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :start AND :end")
    Long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('DATE', a.createdAt) as date, COUNT(a) as count " +
           "FROM AuditLog a " +
           "WHERE a.createdAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', a.createdAt) " +
           "ORDER BY date")
    List<Object[]> getAuditTrailByDay(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT a.actionCategory, COUNT(a) FROM AuditLog a " +
           "WHERE a.createdAt BETWEEN :start AND :end " +
           "GROUP BY a.actionCategory")
    List<Object[]> getActivityByCategory(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a " +
           "WHERE a.createdAt BETWEEN :start AND :end " +
           "GROUP BY a.entityType " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getMostActiveEntities(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end, 
            Pageable pageable);
}