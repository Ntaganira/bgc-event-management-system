package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : AnalyticsService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for analytics and reporting
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.exception.EventException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    
    /**
     * Get dashboard analytics for main overview
     * @return Dashboard analytics
     */
    AnalyticsDashboardDto getDashboardAnalytics();
    
    /**
     * Get detailed analytics for a specific event
     * @param eventId Event ID
     * @return Event analytics
     * @throws EventException if event not found
     */
    EventAnalyticsDto getEventAnalytics(Long eventId) throws EventException;
    
    /**
     * Get analytics for all events with pagination
     * @param pageable Pagination
     * @return Page of event statistics
     */
    Page<EventStatDto> getAllEventsAnalytics(Pageable pageable);
    
    /**
     * Get analytics for events by organizer
     * @param organizerId Organizer ID
     * @param pageable Pagination
     * @return Page of event statistics
     */
    Page<EventStatDto> getOrganizerEventsAnalytics(Long organizerId, Pageable pageable);
    
    /**
     * Get registration trends over time
     * @param eventId Event ID (optional)
     * @param startDate Start date
     * @param endDate End date
     * @param interval Interval (DAY, WEEK, MONTH)
     * @return Time series data
     */
    List<TimeSeriesData> getRegistrationTrends(Long eventId, LocalDateTime startDate, 
                                               LocalDateTime endDate, String interval);
    
    /**
     * Get top performing events
     * @param metric Metric to rank by (registrations, attendance, rate)
     * @param limit Number of results
     * @return List of top events
     */
    List<EventStatDto> getTopEvents(String metric, int limit);
    
    /**
     * Generate report in specified format
     * @param request Report request
     * @return Report file as byte array
     */
    byte[] generateReport(ReportRequestDto request) throws EventException;
    
    /**
     * Export event registrations to PDF
     * @param eventId Event ID
     * @return PDF byte array
     */
    byte[] exportEventRegistrationsToPdf(Long eventId) throws EventException;
    
    /**
     * Export event registrations to Excel
     * @param eventId Event ID
     * @return Excel byte array
     */
    byte[] exportEventRegistrationsToExcel(Long eventId) throws EventException;
    
    /**
     * Export all events summary to Excel
     * @return Excel byte array
     */
    byte[] exportAllEventsSummaryToExcel();
    
    /**
     * Get system health metrics
     * @return System health
     */
    SystemHealthDto getSystemHealth();
    
    /**
     * Get audit logs for reporting
     * @param startDate Start date
     * @param endDate End date
     * @param entityType Entity type filter
     * @param pageable Pagination
     * @return Page of audit logs
     */
    Page<AuditLogDto> getAuditLogs(LocalDateTime startDate, LocalDateTime endDate,
                                   String entityType, Pageable pageable);
}