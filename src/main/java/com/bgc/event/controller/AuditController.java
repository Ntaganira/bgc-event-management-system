package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AuditController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for audit log management
 * </pre>
 */

import com.bgc.event.dto.AuditLogDto;
import com.bgc.event.dto.AuditSummaryDto;
import com.bgc.event.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    
    private final AuditService auditService;
    
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actionCategory,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("REST request to get audit logs with filters");
        
        Page<AuditLogDto> logs = auditService.searchAuditLogs(
                startDate, endDate, userId, entityType, actionCategory, search, pageable);
        
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditSummaryDto> getAuditSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("REST request to get audit summary from {} to {}", startDate, endDate);
        
        AuditSummaryDto summary = auditService.getAuditSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/user/{userId}/timeline")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Map<String, Long>> getUserActivityTimeline(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("REST request to get activity timeline for user ID: {}, days: {}", userId, days);
        
        Map<String, Long> timeline = auditService.getUserActivityTimeline(userId, days);
        return ResponseEntity.ok(timeline);
    }
    
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "csv") String format) {
        
        log.info("REST request to export audit logs in {} format", format);
        
        // This would call a service to generate export
        byte[] exportData = new byte[0]; // Placeholder
        
        HttpHeaders headers = new HttpHeaders();
        String filename = "audit_logs_" + 
                         startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                         endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "." + format;
        
        if ("csv".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.parseMediaType("text/csv"));
        } else if ("json".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
    }
    
    @DeleteMapping("/clean")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanOldLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        
        log.info("REST request to clean audit logs before {}", before);
        
        int deletedCount = auditService.cleanOldLogs(before);
        
        return ResponseEntity.ok(Map.of(
                "message", "Old audit logs cleaned successfully",
                "deletedCount", deletedCount,
                "before", before
        ));
    }
}