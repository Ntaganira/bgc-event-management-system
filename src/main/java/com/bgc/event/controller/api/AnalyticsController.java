package com.bgc.event.controller.api;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AnalyticsController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for analytics and reporting endpoints
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.exception.EventException;
import com.bgc.event.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<AnalyticsDashboardDto> getDashboardAnalytics() {
        log.info("REST request to get dashboard analytics");
        AnalyticsDashboardDto dashboard = analyticsService.getDashboardAnalytics();
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventAnalyticsDto> getEventAnalytics(@PathVariable Long eventId) 
            throws EventException {
        log.info("REST request to get analytics for event ID: {}", eventId);
        EventAnalyticsDto analytics = analyticsService.getEventAnalytics(eventId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<EventStatDto>> getAllEventsAnalytics(
            @PageableDefault(size = 20, sort = "eventDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all events analytics");
        Page<EventStatDto> stats = analyticsService.getAllEventsAnalytics(pageable);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/organizer/{organizerId}/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<EventStatDto>> getOrganizerEventsAnalytics(
            @PathVariable Long organizerId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("REST request to get events analytics for organizer ID: {}", organizerId);
        Page<EventStatDto> stats = analyticsService.getOrganizerEventsAnalytics(organizerId, pageable);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/trends/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<TimeSeriesData>> getRegistrationTrends(
            @RequestParam(required = false) Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "DAY") String interval) {
        
        log.info("REST request to get registration trends");
        List<TimeSeriesData> trends = analyticsService.getRegistrationTrends(
                eventId, startDate, endDate, interval);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/top-events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<EventStatDto>> getTopEvents(
            @RequestParam(defaultValue = "registrations") String metric,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("REST request to get top events by: {}", metric);
        List<EventStatDto> topEvents = analyticsService.getTopEvents(metric, limit);
        return ResponseEntity.ok(topEvents);
    }
    
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> generateReport(@Valid @RequestBody ReportRequestDto request) 
            throws EventException {
        
        log.info("REST request to generate report: {}", request.getReportType());
        byte[] reportData = analyticsService.generateReport(request);
        
        HttpHeaders headers = new HttpHeaders();
        String filename = generateFilename(request);
        
        switch (request.getFormat().toUpperCase()) {
            case "PDF":
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename + ".pdf");
                break;
            case "EXCEL":
                headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
                headers.setContentDispositionFormData("attachment", filename + ".xlsx");
                break;
            case "CSV":
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", filename + ".csv");
                break;
        }
        
        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }
    
    @GetMapping("/events/{eventId}/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> exportEventRegistrationsToPdf(@PathVariable Long eventId) 
            throws EventException {
        
        log.info("REST request to export event registrations to PDF: {}", eventId);
        byte[] pdfData = analyticsService.exportEventRegistrationsToPdf(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "event-" + eventId + "-registrations.pdf");
        
        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }
    
    @GetMapping("/events/{eventId}/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> exportEventRegistrationsToExcel(@PathVariable Long eventId) 
            throws EventException {
        
        log.info("REST request to export event registrations to Excel: {}", eventId);
        byte[] excelData = analyticsService.exportEventRegistrationsToExcel(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "event-" + eventId + "-registrations.xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
    
    @GetMapping("/export/events-summary")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<byte[]> exportAllEventsSummary() {
        log.info("REST request to export all events summary to Excel");
        byte[] excelData = analyticsService.exportAllEventsSummaryToExcel();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "all-events-summary.xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
    
    @GetMapping("/system-health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemHealthDto> getSystemHealth() {
        log.info("REST request to get system health");
        SystemHealthDto health = analyticsService.getSystemHealth();
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 50) Pageable pageable) {
        
        log.info("REST request to get audit logs");
        Page<AuditLogDto> logs = analyticsService.getAuditLogs(startDate, endDate, entityType, pageable);
        return ResponseEntity.ok(logs);
    }
    
    private String generateFilename(ReportRequestDto request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return request.getReportType().toString().toLowerCase() + "_report_" + timestamp;
    }
}