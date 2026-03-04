package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : ExportController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Download endpoints for Excel (.xlsx) and PDF exports.
 *                All endpoints require VIEW_* or MANAGE_* authority.
 *
 *  GET /export/events/excel
 *  GET /export/events/pdf
 *  GET /export/attendance/excel
 *  GET /export/attendance/pdf
 *  GET /export/users/excel
 *  GET /export/users/pdf
 *  GET /export/audit/excel
 *  GET /export/audit/pdf
 * </pre>
 */

import com.bgc.event.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    // ── Events ──────────────────────────────────────────────────────────
    @GetMapping("/events/excel")
    @PreAuthorize("hasAuthority('VIEW_EVENT')")
    public void eventsExcel(HttpServletResponse response) throws Exception {
        log.info("Exporting events → Excel");
        exportService.exportEventsExcel(response);
    }

    @GetMapping("/events/pdf")
    @PreAuthorize("hasAuthority('VIEW_EVENT')")
    public void eventsPdf(HttpServletResponse response) throws Exception {
        log.info("Exporting events → PDF");
        exportService.exportEventsPdf(response);
    }

    // ── Attendance ───────────────────────────────────────────────────────
    @GetMapping("/attendance/excel")
    @PreAuthorize("hasAuthority('VIEW_ATTENDANCE')")
    public void attendanceExcel(HttpServletResponse response) throws Exception {
        log.info("Exporting attendance → Excel");
        exportService.exportAttendanceExcel(response);
    }

    @GetMapping("/attendance/pdf")
    @PreAuthorize("hasAuthority('VIEW_ATTENDANCE')")
    public void attendancePdf(HttpServletResponse response) throws Exception {
        log.info("Exporting attendance → PDF");
        exportService.exportAttendancePdf(response);
    }

    // ── Users ────────────────────────────────────────────────────────────
    @GetMapping("/users/excel")
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public void usersExcel(HttpServletResponse response) throws Exception {
        log.info("Exporting users → Excel");
        exportService.exportUsersExcel(response);
    }

    @GetMapping("/users/pdf")
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public void usersPdf(HttpServletResponse response) throws Exception {
        log.info("Exporting users → PDF");
        exportService.exportUsersPdf(response);
    }

    // ── Audit Logs ───────────────────────────────────────────────────────
    @GetMapping("/audit/excel")
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOGS')")
    public void auditExcel(HttpServletResponse response) throws Exception {
        log.info("Exporting audit logs → Excel");
        exportService.exportAuditExcel(response);
    }

    @GetMapping("/audit/pdf")
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOGS')")
    public void auditPdf(HttpServletResponse response) throws Exception {
        log.info("Exporting audit logs → PDF");
        exportService.exportAuditPdf(response);
    }
}
