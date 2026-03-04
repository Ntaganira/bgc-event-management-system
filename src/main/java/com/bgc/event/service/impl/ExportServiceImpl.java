package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : ExportServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Export Events, Attendance, Users and Audit Logs
 *                to Excel (.xlsx via Apache POI) and PDF (OpenPDF).
 * </pre>
 */

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.AuditLog;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.repository.AttendanceRepository;
import com.bgc.event.repository.AuditLogRepository;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.ExportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final EventRepository      eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository       userRepository;
    private final AuditLogRepository   auditLogRepository;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ════════════════════════════════════════════════════════════════════════
    // EXCEL EXPORTS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void exportEventsExcel(HttpServletResponse response) throws Exception {
        List<Event> events = eventRepository.findAll();
        String[] headers = {"#", "Title", "Location", "Start Date", "End Date",
                            "Created By", "Attendance Count", "Created At"};

        try (XSSFWorkbook wb = newWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Events");
            buildExcelSheet(wb, sheet, "BGC Event Management — Events Export", headers, events.size(), (rowIdx, row) -> {
                Event e = events.get(rowIdx);
                setCells(row,
                    String.valueOf(rowIdx + 1),
                    e.getTitle(),
                    e.getLocation() != null ? e.getLocation() : "—",
                    e.getStartDateTime() != null ? e.getStartDateTime().format(DT_FMT) : "—",
                    e.getEndDateTime()   != null ? e.getEndDateTime().format(DT_FMT)   : "—",
                    e.getCreatedBy()     != null ? e.getCreatedBy().getFullName()       : "—",
                    String.valueOf(e.getAttendanceRecords().size()),
                    e.getCreatedAt()     != null ? e.getCreatedAt().format(DT_FMT)     : "—"
                );
            });
            autoSizeColumns(sheet, headers.length);
            writeExcel(response, wb, "events");
        }
    }

    @Override
    public void exportAttendanceExcel(HttpServletResponse response) throws Exception {
        List<Attendance> records = attendanceRepository.findAll();
        String[] headers = {"#", "User Name", "Email", "Event Title", "Method", "Attendance Time"};

        try (XSSFWorkbook wb = newWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Attendance");
            buildExcelSheet(wb, sheet, "BGC Event Management — Attendance Export", headers, records.size(), (rowIdx, row) -> {
                Attendance a = records.get(rowIdx);
                setCells(row,
                    String.valueOf(rowIdx + 1),
                    a.getUser().getFullName(),
                    a.getUser().getEmail(),
                    a.getEvent().getTitle(),
                    a.getMethod().name(),
                    a.getAttendanceTime() != null ? a.getAttendanceTime().format(DT_FMT) : "—"
                );
            });
            autoSizeColumns(sheet, headers.length);
            writeExcel(response, wb, "attendance");
        }
    }

    @Override
    public void exportUsersExcel(HttpServletResponse response) throws Exception {
        List<User> users = userRepository.findAll();
        String[] headers = {"#", "First Name", "Last Name", "Email", "Phone",
                            "Office / Branch", "Title", "Roles", "Status",
                            "Arrival Date", "Return Date", "Created At"};

        try (XSSFWorkbook wb = newWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Users");
            buildExcelSheet(wb, sheet, "BGC Event Management — Users Export", headers, users.size(), (rowIdx, row) -> {
                User u = users.get(rowIdx);
                String roles = u.getRoles().stream()
                    .map(r -> r.getName().replace("ROLE_", ""))
                    .reduce((a, b) -> a + ", " + b).orElse("—");
                setCells(row,
                    String.valueOf(rowIdx + 1),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getEmail(),
                    u.getPhoneNumber() != null ? u.getPhoneNumber() : "—",
                    u.getOfficeName(),
                    u.getTitle()       != null ? u.getTitle()       : "—",
                    roles,
                    u.isEnabled() ? "Active" : "Disabled",
                    u.getArrivalDate() != null ? u.getArrivalDate().format(D_FMT) : "—",
                    u.getReturnDate()  != null ? u.getReturnDate().format(D_FMT)  : "—",
                    u.getCreatedAt()   != null ? u.getCreatedAt().format(DT_FMT)  : "—"
                );
            });
            autoSizeColumns(sheet, headers.length);
            writeExcel(response, wb, "users");
        }
    }

    @Override
    public void exportAuditExcel(HttpServletResponse response) throws Exception {
        List<AuditLog> logs = auditLogRepository.findAll();
        String[] headers = {"#", "Username", "Action", "Entity", "Entity ID", "Details", "IP Address", "Timestamp"};

        try (XSSFWorkbook wb = newWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Audit Logs");
            buildExcelSheet(wb, sheet, "BGC Event Management — Audit Log Export", headers, logs.size(), (rowIdx, row) -> {
                AuditLog l = logs.get(rowIdx);
                setCells(row,
                    String.valueOf(rowIdx + 1),
                    l.getUsername()  != null ? l.getUsername()   : "—",
                    l.getAction(),
                    l.getEntityName() != null ? l.getEntityName() : "—",
                    l.getEntityId()   != null ? l.getEntityId()   : "—",
                    l.getDetails()    != null ? l.getDetails()    : "—",
                    l.getIpAddress()  != null ? l.getIpAddress()  : "—",
                    l.getCreatedAt()  != null ? l.getCreatedAt().format(DT_FMT) : "—"
                );
            });
            autoSizeColumns(sheet, headers.length);
            writeExcel(response, wb, "audit-logs");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // PDF EXPORTS
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void exportEventsPdf(HttpServletResponse response) throws Exception {
        List<Event> events = eventRepository.findAll();
        float[] widths = {0.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 0.8f};
        String[] headers = {"#", "Title", "Location", "Start", "End", "Created By", "Attend."};

        writePdf(response, "events", "Events Export", headers, widths, events.size(), rowIdx -> {
            Event e = events.get(rowIdx);
            return new String[]{
                String.valueOf(rowIdx + 1),
                e.getTitle(),
                e.getLocation() != null ? e.getLocation() : "—",
                e.getStartDateTime() != null ? e.getStartDateTime().format(DT_FMT) : "—",
                e.getEndDateTime()   != null ? e.getEndDateTime().format(DT_FMT)   : "—",
                e.getCreatedBy()     != null ? e.getCreatedBy().getFullName()       : "—",
                String.valueOf(e.getAttendanceRecords().size())
            };
        });
    }

    @Override
    public void exportAttendancePdf(HttpServletResponse response) throws Exception {
        List<Attendance> records = attendanceRepository.findAll();
        float[] widths = {0.5f, 2f, 2.5f, 2f, 1f, 2f};
        String[] headers = {"#", "Name", "Email", "Event", "Method", "Time"};

        writePdf(response, "attendance", "Attendance Export", headers, widths, records.size(), rowIdx -> {
            Attendance a = records.get(rowIdx);
            return new String[]{
                String.valueOf(rowIdx + 1),
                a.getUser().getFullName(),
                a.getUser().getEmail(),
                a.getEvent().getTitle(),
                a.getMethod().name(),
                a.getAttendanceTime() != null ? a.getAttendanceTime().format(DT_FMT) : "—"
            };
        });
    }

    @Override
    public void exportUsersPdf(HttpServletResponse response) throws Exception {
        List<User> users = userRepository.findAll();
        float[] widths = {0.5f, 1.5f, 1.5f, 2.5f, 1.5f, 1.5f, 1f};
        String[] headers = {"#", "First Name", "Last Name", "Email", "Office", "Roles", "Status"};

        writePdf(response, "users", "Users Export", headers, widths, users.size(), rowIdx -> {
            User u = users.get(rowIdx);
            String roles = u.getRoles().stream()
                .map(r -> r.getName().replace("ROLE_", ""))
                .reduce((a, b) -> a + ", " + b).orElse("—");
            return new String[]{
                String.valueOf(rowIdx + 1),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getOfficeName(),
                roles,
                u.isEnabled() ? "Active" : "Disabled"
            };
        });
    }

    @Override
    public void exportAuditPdf(HttpServletResponse response) throws Exception {
        List<AuditLog> logs = auditLogRepository.findAll();
        float[] widths = {0.5f, 1.8f, 1.8f, 1.2f, 1f, 1.5f, 2f};
        String[] headers = {"#", "Username", "Action", "Entity", "Entity ID", "IP", "Timestamp"};

        writePdf(response, "audit-logs", "Audit Log Export", headers, widths, logs.size(), rowIdx -> {
            AuditLog l = logs.get(rowIdx);
            return new String[]{
                String.valueOf(rowIdx + 1),
                l.getUsername()   != null ? l.getUsername()   : "—",
                l.getAction(),
                l.getEntityName() != null ? l.getEntityName() : "—",
                l.getEntityId()   != null ? l.getEntityId()   : "—",
                l.getIpAddress()  != null ? l.getIpAddress()  : "—",
                l.getCreatedAt()  != null ? l.getCreatedAt().format(DT_FMT) : "—"
            };
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXCEL HELPERS
    // ════════════════════════════════════════════════════════════════════════

    @FunctionalInterface interface RowFiller { void fill(int rowIdx, XSSFRow row); }

    private XSSFWorkbook newWorkbook() { return new XSSFWorkbook(); }

    private void buildExcelSheet(XSSFWorkbook wb, XSSFSheet sheet,
                                 String title, String[] headers,
                                 int dataCount, RowFiller filler) {
        // ── Brand colours ────────────────────────────────────────────────
        XSSFColor navy   = xssfColor(30,  58,  95);
        XSSFColor blue   = xssfColor(37,  99, 235);
        XSSFColor altRow = xssfColor(241, 245, 249);
        XSSFColor white  = xssfColor(255, 255, 255);
        XSSFColor dark   = xssfColor(17,  24,  39);

        // ── Title row ────────────────────────────────────────────────────
        XSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(32);
        XSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFillForegroundColor(navy);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBold(true);
        titleFont.setColor(white);
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setAlignment(HorizontalAlignment.LEFT);
        titleStyle.setIndention((short) 1);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

        // ── Header row ───────────────────────────────────────────────────
        XSSFRow headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(22);
        XSSFCellStyle hdrStyle = wb.createCellStyle();
        hdrStyle.setFillForegroundColor(blue);
        hdrStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont hdrFont = wb.createFont();
        hdrFont.setFontName("Arial");
        hdrFont.setFontHeightInPoints((short) 11);
        hdrFont.setBold(true);
        hdrFont.setColor(white);
        hdrStyle.setFont(hdrFont);
        hdrStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        hdrStyle.setAlignment(HorizontalAlignment.CENTER);
        hdrStyle.setBorderBottom(BorderStyle.THIN);
        hdrStyle.setBottomBorderColor(navy);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(hdrStyle);
        }

        // ── Data row styles ───────────────────────────────────────────────
        XSSFCellStyle evenStyle = dataRowStyle(wb, white,   dark);
        XSSFCellStyle oddStyle  = dataRowStyle(wb, altRow,  dark);

        // ── Data rows ────────────────────────────────────────────────────
        for (int i = 0; i < dataCount; i++) {
            XSSFRow row = sheet.createRow(i + 2);
            row.setHeightInPoints(18);
            filler.fill(i, row);
            XSSFCellStyle style = (i % 2 == 0) ? evenStyle : oddStyle;
            for (int c = 0; c < headers.length; c++) {
                Cell cell = row.getCell(c);
                if (cell != null) cell.setCellStyle(style);
            }
        }

        // ── Summary row ──────────────────────────────────────────────────
        int summaryRowIdx = dataCount + 2;
        XSSFRow summaryRow = sheet.createRow(summaryRowIdx);
        summaryRow.setHeightInPoints(18);
        XSSFCell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue("Total records: " + dataCount);
        XSSFCellStyle summaryStyle = wb.createCellStyle();
        summaryStyle.setFillForegroundColor(navy);
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont summaryFont = wb.createFont();
        summaryFont.setFontName("Arial");
        summaryFont.setFontHeightInPoints((short) 10);
        summaryFont.setBold(true);
        summaryFont.setColor(white);
        summaryStyle.setFont(summaryFont);
        summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        summaryCell.setCellStyle(summaryStyle);
        sheet.addMergedRegion(new CellRangeAddress(summaryRowIdx, summaryRowIdx, 0, headers.length - 1));
    }

    private XSSFCellStyle dataRowStyle(XSSFWorkbook wb, XSSFColor bg, XSSFColor fg) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(bg);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 10);
        f.setColor(fg);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(xssfColor(226, 232, 240));
        return s;
    }

    private void setCells(XSSFRow row, String... values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i] != null ? values[i] : "—");
        }
    }

    private void autoSizeColumns(XSSFSheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            // add a little padding
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 15000));
        }
    }

    private void writeExcel(HttpServletResponse response, XSSFWorkbook wb, String name) throws Exception {
        String filename = "bgc-" + name + "-export.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        wb.write(response.getOutputStream());
        response.getOutputStream().flush();
    }

    private XSSFColor xssfColor(int r, int g, int b) {
        return new XSSFColor(new java.awt.Color(r, g, b), null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // PDF HELPERS
    // ════════════════════════════════════════════════════════════════════════

    @FunctionalInterface interface PdfRowProvider { String[] get(int rowIdx); }

    private static final Color PDF_NAVY   = new Color(30,  58,  95);
    private static final Color PDF_BLUE   = new Color(37,  99, 235);
    private static final Color PDF_ALT    = new Color(241, 245, 249);
    private static final Color PDF_WHITE  = Color.WHITE;
    private static final Color PDF_DARK   = new Color(17,  24,  39);
    private static final Color PDF_BORDER = new Color(203, 213, 225);

    private void writePdf(HttpServletResponse response,
                          String name, String sheetTitle,
                          String[] headers, float[] widths,
                          int dataCount, PdfRowProvider provider) throws Exception {

        String filename = "bgc-" + name + "-export.pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        PdfWriter writer = PdfWriter.getInstance(doc, response.getOutputStream());

        // ── Header / Footer ───────────────────────────────────────────────
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter w, Document d) {
                PdfContentByte cb = w.getDirectContent();
                // top line
                cb.setColorStroke(PDF_BLUE);
                cb.setLineWidth(3f);
                cb.moveTo(d.left(), d.top() + 10);
                cb.lineTo(d.right(), d.top() + 10);
                cb.stroke();
                // bottom line
                cb.setColorStroke(PDF_BORDER);
                cb.setLineWidth(0.5f);
                cb.moveTo(d.left(), d.bottom() - 15);
                cb.lineTo(d.right(), d.bottom() - 15);
                cb.stroke();
                // page number
                Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
                Phrase pg = new Phrase("BGC Event Management System  ·  Page " + w.getPageNumber(), footerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pg, d.right(), d.bottom() - 25, 0);
            }
        });

        doc.open();

        // ── Title ─────────────────────────────────────────────────────────
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, PDF_NAVY);
        Paragraph titlePara = new Paragraph("BGC Event Management — " + sheetTitle, titleFont);
        titlePara.setSpacingAfter(4);
        doc.add(titlePara);

        Font subFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
        Paragraph subPara = new Paragraph("Total records: " + dataCount + "  ·  Exported on: " +
            java.time.LocalDateTime.now().format(DT_FMT), subFont);
        subPara.setSpacingAfter(12);
        doc.add(subPara);

        // ── Table ─────────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setHeaderRows(1);
        table.setSpacingBefore(0);

        // Header cells
        Font hdrFont = new Font(Font.HELVETICA, 9, Font.BOLD, PDF_WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hdrFont));
            cell.setBackgroundColor(PDF_BLUE);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(PDF_NAVY);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }

        // Data rows
        Font dataFont  = new Font(Font.HELVETICA, 8, Font.NORMAL, PDF_DARK);
        for (int i = 0; i < dataCount; i++) {
            String[] row  = provider.get(i);
            Color    bg   = (i % 2 == 0) ? PDF_WHITE : PDF_ALT;
            for (int c = 0; c < row.length; c++) {
                PdfPCell cell = new PdfPCell(new Phrase(row[c] != null ? row[c] : "—", dataFont));
                cell.setBackgroundColor(bg);
                cell.setPadding(5);
                cell.setHorizontalAlignment(c == 0 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                cell.setBorderColor(PDF_BORDER);
                cell.setBorderWidth(0.3f);
                table.addCell(cell);
            }
        }

        doc.add(table);
        doc.close();
    }
}
