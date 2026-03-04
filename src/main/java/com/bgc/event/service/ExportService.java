package com.bgc.event.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ExportService {

    // ── Excel ──────────────────────────────────────────────────────────────
    void exportEventsExcel(HttpServletResponse response)     throws Exception;
    void exportAttendanceExcel(HttpServletResponse response) throws Exception;
    void exportUsersExcel(HttpServletResponse response)      throws Exception;
    void exportAuditExcel(HttpServletResponse response)      throws Exception;

    // ── PDF ────────────────────────────────────────────────────────────────
    void exportEventsPdf(HttpServletResponse response)     throws Exception;
    void exportAttendancePdf(HttpServletResponse response) throws Exception;
    void exportUsersPdf(HttpServletResponse response)      throws Exception;
    void exportAuditPdf(HttpServletResponse response)      throws Exception;
}
