package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : AuditService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Enhanced Audit Service interface for FR-27
 * </pre>
 */

import com.bgc.event.dto.AuditLogDto;
import com.bgc.event.dto.AuditSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

public interface AuditService {
    
    /**
     * Log an action with full details
     * @param action Action performed
     * @param userId User ID (can be null for public actions)
     * @param entityType Type of entity affected
     * @param entityId ID of entity affected
     * @param entityName Human-readable name of entity
     * @param oldValues Previous values (for updates)
     * @param newValues New values (for creates/updates)
     * @param request HttpServletRequest for IP and user agent
     * @param status Status of the action
     * @param errorMessage Error message if failed
     * @param executionTimeMs Execution time in milliseconds
     */
    void logAction(
            String action,
            Long userId,
            String entityType,
            Long entityId,
            String entityName,
            Object oldValues,
            Object newValues,
            HttpServletRequest request,
            String status,
            String errorMessage,
            Long executionTimeMs);
    
    /**
     * Simplified log action with automatic request detection
     */
    void logAction(String action, Long userId, String entityType, Long entityId, String details);
    
    /**
     * Log successful action with automatic change detection
     */
    void logSuccess(String action, Long userId, String entityType, Long entityId, 
                    String entityName, Object oldValues, Object newValues);
    
    /**
     * Log failed action
     */
    void logFailure(String action, Long userId, String entityType, Long entityId, 
                    String errorMessage, Exception exception);
    
    /**
     * Log login attempt
     */
    void logLogin(String email, boolean success, String failureReason, HttpServletRequest request);
    
    /**
     * Log export action
     */
    void logExport(Long userId, String entityType, String format, int recordCount);
    
    /**
     * Search audit logs with filters
     */
    Page<AuditLogDto> searchAuditLogs(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long userId,
            String entityType,
            String actionCategory,
            String searchTerm,
            Pageable pageable);
    
    /**
     * Get audit summary for dashboard
     */
    AuditSummaryDto getAuditSummary(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get user activity timeline
     */
    Map<String, Long> getUserActivityTimeline(Long userId, int days);
    
    /**
     * Clean old audit logs (admin only)
     */
    int cleanOldLogs(LocalDateTime before);
}