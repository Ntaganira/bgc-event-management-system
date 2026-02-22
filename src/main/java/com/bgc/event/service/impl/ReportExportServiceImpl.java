package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : ReportExportServiceImpl.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Enhanced implementation of Report Export Service with iText 7 and Apache POI
 * </pre>
 */

import com.bgc.event.dto.ReportRequestDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import com.bgc.event.entity.User;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.ReportExportService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;  // iText Cell
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {
    
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_FORMAT = 
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    // Color constants for branding
    private static final Color PRIMARY_COLOR = new DeviceRgb(26, 35, 126); // Dark blue
    private static final Color SECONDARY_COLOR = new DeviceRgb(40, 53, 147); // Light blue
    private static final Color SUCCESS_COLOR = new DeviceRgb(40, 167, 69); // Green
    private static final Color WARNING_COLOR = new DeviceRgb(255, 193, 7); // Yellow
    private static final Color DANGER_COLOR = new DeviceRgb(220, 53, 69); // Red
    
    // ==================== PDF GENERATION METHODS (iText) ====================
    
    @Override
    public byte[] generatePdfReport(ReportRequestDto request) {
        log.info("Generating PDF report: {}", request.getReportType());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);
            
            // Add report header
            addReportHeader(document, request);
            
            // Add content based on report type
            switch (request.getReportType()) {
                case EVENTS_SUMMARY:
                    addEventsSummaryPdf(document, request);
                    break;
                case REGISTRATIONS_DETAILED:
                    addRegistrationsDetailedPdf(document, request);
                    break;
                case ATTENDANCE_REPORT:
                    addAttendanceReportPdf(document, request);
                    break;
                case ORGANIZER_PERFORMANCE:
                    addOrganizerPerformancePdf(document, request);
                    break;
                case SYSTEM_AUDIT:
                    addSystemAuditPdf(document, request);
                    break;
                default:
                    addDefaultReportPdf(document, request);
            }
            
            // Add footer
            addReportFooter(document);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] exportRegistrationsToPdf(Event event, List<Registration> registrations) {
        log.info("Exporting {} registrations to PDF for event: {}", registrations.size(), event.getTitle());
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());
            
            // Title
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            
            Paragraph title = new Paragraph("Registrations Report: " + event.getTitle())
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            // Event details
            Paragraph eventDetails = new Paragraph()
                    .add(new Paragraph("Date: " + event.getStartDate().format(DATE_FORMAT)).setFont(normalFont))
                    .add(new Paragraph("Venue: " + event.getVenue()).setFont(normalFont))
                    .add(new Paragraph("Total Registrations: " + registrations.size()).setFont(normalFont));
            document.add(eventDetails);
            
            document.add(new Paragraph("\n"));
            
            // Create table
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3, 3, 2, 2, 1, 1}))
                    .useAllAvailableWidth();
            
            // Table headers
            addPdfTableHeader(table, "#", "Name", "Email", "Organization", "Status", "Checked In", "Time", "Actions");
            
            // Table data
            int count = 1;
            for (Registration reg : registrations) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(count++))).setFont(normalFont));
                table.addCell(new Cell().add(new Paragraph(reg.getFirstName() + " " + reg.getLastName())).setFont(normalFont));
                table.addCell(new Cell().add(new Paragraph(reg.getEmail())).setFont(normalFont));
                table.addCell(new Cell().add(new Paragraph(reg.getOrganization() != null ? reg.getOrganization() : "-")).setFont(normalFont));
                
                // Status with color
                Cell statusCell = new Cell().add(new Paragraph(reg.getStatus().toString())).setFont(normalFont);
                if (reg.getStatus() == Registration.RegistrationStatus.CONFIRMED) {
                    statusCell.setFontColor(SUCCESS_COLOR);
                } else if (reg.getStatus() == Registration.RegistrationStatus.CANCELLED) {
                    statusCell.setFontColor(DANGER_COLOR);
                } else if (reg.getStatus() == Registration.RegistrationStatus.WAITLISTED) {
                    statusCell.setFontColor(WARNING_COLOR);
                }
                table.addCell(statusCell);
                
                table.addCell(new Cell().add(new Paragraph(reg.isCheckedIn() ? "✓ Yes" : "✗ No")).setFont(normalFont));
                table.addCell(new Cell().add(new Paragraph(reg.getCheckedInAt() != null ? 
                        reg.getCheckedInAt().format(DATE_FORMAT) : "-")).setFont(normalFont));
                table.addCell(new Cell().add(new Paragraph("View")).setFont(normalFont).setFontColor(PRIMARY_COLOR));
            }
            
            document.add(table);
            
            // Summary
            long checkedIn = registrations.stream().filter(Registration::isCheckedIn).count();
            Paragraph summary = new Paragraph()
                    .add(new Paragraph("\nSummary:").setFont(boldFont))
                    .add(new Paragraph("Total: " + registrations.size()).setFont(normalFont))
                    .add(new Paragraph("Checked In: " + checkedIn).setFont(normalFont))
                    .add(new Paragraph("Attendance Rate: " + 
                            String.format("%.1f%%", (checkedIn * 100.0 / registrations.size()))).setFont(normalFont));
            document.add(summary);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to export registrations to PDF", e);
            throw new RuntimeException("Failed to export registrations to PDF", e);
        }
    }
    
    // ==================== EXCEL GENERATION METHODS (Apache POI) ====================
    
    @Override
    public byte[] generateExcelReport(ReportRequestDto request) {
        log.info("Generating Excel report: {}", request.getReportType());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Create styles
            Map<String, org.apache.poi.ss.usermodel.CellStyle> styles = createExcelStyles(workbook);
            
            // Create sheets based on report type
            switch (request.getReportType()) {
                case EVENTS_SUMMARY:
                    createEventsSummarySheet(workbook, styles, request);
                    break;
                case REGISTRATIONS_DETAILED:
                    createRegistrationsDetailedSheet(workbook, styles, request);
                    break;
                case ATTENDANCE_REPORT:
                    createAttendanceReportSheet(workbook, styles, request);
                    break;
                case ORGANIZER_PERFORMANCE:
                    createOrganizerPerformanceSheet(workbook, styles, request);
                    break;
                case SYSTEM_AUDIT:
                    createSystemAuditSheet(workbook, styles, request);
                    break;
                default:
                    createDefaultSheet(workbook, styles, request);
            }
            
            // Create summary sheet
            createSummarySheet(workbook, styles, request);
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
    
    @Override
    public byte[] exportRegistrationsToExcel(Event event, List<Registration> registrations) {
        log.info("Exporting {} registrations to Excel for event: {}", registrations.size(), event.getTitle());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Create styles
            Map<String, org.apache.poi.ss.usermodel.CellStyle> styles = createExcelStyles(workbook);
            
            // Create main sheet
            Sheet sheet = workbook.createSheet("Registrations");
            
            // Set column widths
            sheet.setColumnWidth(0, 5 * 256);   // #
            sheet.setColumnWidth(1, 20 * 256);  // Name
            sheet.setColumnWidth(2, 30 * 256);  // Email
            sheet.setColumnWidth(3, 15 * 256);  // Phone
            sheet.setColumnWidth(4, 20 * 256);  // Organization
            sheet.setColumnWidth(5, 15 * 256);  // Job Title
            sheet.setColumnWidth(6, 15 * 256);  // Status
            sheet.setColumnWidth(7, 10 * 256);  // Checked In
            sheet.setColumnWidth(8, 20 * 256);  // Check-in Time
            sheet.setColumnWidth(9, 20 * 256);  // Registered At
            
            // Title row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Event Registrations: " + event.getTitle());
            titleCell.setCellStyle(styles.get("title"));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            
            // Event info row
            Row infoRow = sheet.createRow(1);
            org.apache.poi.ss.usermodel.Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Date: " + event.getStartDate().format(DATE_FORMAT) + 
                                 " | Venue: " + event.getVenue() + 
                                 " | Capacity: " + (event.getCapacity() != null ? event.getCapacity() : "Unlimited"));
            infoCell.setCellStyle(styles.get("info"));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));
            
            // Headers row
            Row headerRow = sheet.createRow(2);
            String[] headers = {"#", "Name", "Email", "Phone", "Organization", 
                               "Job Title", "Status", "Checked In", "Check-in Time", "Registered At"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styles.get("header"));
            }
            
            // Data rows
            int rowNum = 3;
            for (Registration reg : registrations) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(rowNum - 3);
                row.createCell(1).setCellValue(reg.getFirstName() + " " + reg.getLastName());
                row.createCell(2).setCellValue(reg.getEmail());
                row.createCell(3).setCellValue(reg.getPhoneNumber() != null ? reg.getPhoneNumber() : "");
                row.createCell(4).setCellValue(reg.getOrganization() != null ? reg.getOrganization() : "");
                row.createCell(5).setCellValue(reg.getJobTitle() != null ? reg.getJobTitle() : "");
                
                org.apache.poi.ss.usermodel.Cell statusCell = row.createCell(6);
                statusCell.setCellValue(reg.getStatus().toString());
                statusCell.setCellStyle(styles.get("status_" + reg.getStatus().toString().toLowerCase()));
                
                org.apache.poi.ss.usermodel.Cell checkedInCell = row.createCell(7);
                checkedInCell.setCellValue(reg.isCheckedIn() ? "Yes" : "No");
                checkedInCell.setCellStyle(reg.isCheckedIn() ? styles.get("success") : styles.get("neutral"));
                
                row.createCell(8).setCellValue(reg.getCheckedInAt() != null ? 
                        reg.getCheckedInAt().format(DATE_FORMAT) : "");
                row.createCell(9).setCellValue(reg.getCreatedAt().format(DATE_FORMAT));
            }
            
            // Summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            summaryRow.createCell(0).setCellValue("Summary");
            summaryRow.getCell(0).setCellStyle(styles.get("header"));
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 1));
            
            Row statsRow = sheet.createRow(rowNum + 2);
            statsRow.createCell(0).setCellValue("Total Registrations:");
            statsRow.createCell(1).setCellValue(registrations.size());
            
            long checkedIn = registrations.stream().filter(Registration::isCheckedIn).count();
            statsRow.createCell(2).setCellValue("Checked In:");
            statsRow.createCell(3).setCellValue(checkedIn);
            
            statsRow.createCell(4).setCellValue("Attendance Rate:");
            statsRow.createCell(5).setCellValue(String.format("%.1f%%", 
                    registrations.size() > 0 ? (checkedIn * 100.0 / registrations.size()) : 0));
            
            // Create summary sheet
            createRegistrationSummarySheet(workbook, styles, event, registrations);
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to export registrations to Excel", e);
            throw new RuntimeException("Failed to export registrations to Excel", e);
        }
    }
    
    @Override
    public byte[] exportEventsSummaryToExcel(List<Event> events) {
        log.info("Exporting {} events summary to Excel", events.size());
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Map<String, org.apache.poi.ss.usermodel.CellStyle> styles = createExcelStyles(workbook);
            
            Sheet sheet = workbook.createSheet("Events Summary");
            
            // Set column widths
            int[] columnWidths = {8, 30, 20, 20, 25, 15, 10, 15, 15, 15, 20};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256);
            }
            
            // Title
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BGC Events - Complete Events Summary");
            titleCell.setCellStyle(styles.get("title"));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
            
            // Generated date
            Row dateRow = sheet.createRow(1);
            org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
            dateCell.setCellStyle(styles.get("info"));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));
            
            // Headers
            Row headerRow = sheet.createRow(2);
            String[] headers = {"ID", "Title", "Start Date", "End Date", "Venue", "City", 
                               "Capacity", "Registrations", "Checked In", "Status", "Organizer"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styles.get("header"));
            }
            
            // Data
            int rowNum = 3;
            int totalRegistrations = 0;
            int totalCheckedIn = 0;
            
            for (Event event : events) {
                Row row = sheet.createRow(rowNum++);
                
                long registrations = registrationRepository.countByEventIdAndDeletedFalse(event.getId());
                long checkedIn = registrationRepository.countCheckedInByEvent(event.getId());
                
                totalRegistrations += registrations;
                totalCheckedIn += checkedIn;
                
                row.createCell(0).setCellValue(event.getId());
                row.createCell(1).setCellValue(event.getTitle());
                row.createCell(2).setCellValue(event.getStartDate().format(DATE_ONLY_FORMAT));
                row.createCell(3).setCellValue(event.getEndDate().format(DATE_ONLY_FORMAT));
                row.createCell(4).setCellValue(event.getVenue());
                row.createCell(5).setCellValue(event.getCity() != null ? event.getCity() : "");
                row.createCell(6).setCellValue(event.getCapacity() != null ? event.getCapacity() : 0);
                row.createCell(7).setCellValue(registrations);
                row.createCell(8).setCellValue(checkedIn);
                
                org.apache.poi.ss.usermodel.Cell statusCell = row.createCell(9);
                statusCell.setCellValue(event.getStatus().toString());
                statusCell.setCellStyle(styles.get("status_" + event.getStatus().toString().toLowerCase()));
                
                row.createCell(10).setCellValue(event.getOrganizer().getFirstName() + " " + 
                                               event.getOrganizer().getLastName());
            }
            
            // Summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            summaryRow.createCell(0).setCellValue("TOTALS");
            summaryRow.getCell(0).setCellStyle(styles.get("header"));
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 6));
            
            summaryRow.createCell(7).setCellValue(totalRegistrations);
            summaryRow.createCell(8).setCellValue(totalCheckedIn);
            summaryRow.createCell(9).setCellValue(String.format("%.1f%%", 
                    totalRegistrations > 0 ? (totalCheckedIn * 100.0 / totalRegistrations) : 0));
            
            workbook.write(baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Failed to export events summary to Excel", e);
            throw new RuntimeException("Failed to export events summary to Excel", e);
        }
    }
    
    @Override
    public byte[] generateCsvReport(ReportRequestDto request) {
        log.info("Generating CSV report: {}", request.getReportType());
        
        StringBuilder csv = new StringBuilder();
        
        // Add header
        csv.append("BGC Events Report\n");
        csv.append("Report Type,").append(request.getReportType()).append("\n");
        csv.append("Generated,").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        if (request.getStartDate() != null) {
            csv.append("Start Date,").append(request.getStartDate().format(DATE_ONLY_FORMAT)).append("\n");
        }
        if (request.getEndDate() != null) {
            csv.append("End Date,").append(request.getEndDate().format(DATE_ONLY_FORMAT)).append("\n");
        }
        csv.append("\n");
        
        // Add data based on report type
        switch (request.getReportType()) {
            case EVENTS_SUMMARY:
                generateEventsSummaryCsv(csv, request);
                break;
            case REGISTRATIONS_DETAILED:
                generateRegistrationsDetailedCsv(csv, request);
                break;
            case ATTENDANCE_REPORT:
                generateAttendanceReportCsv(csv, request);
                break;
            default:
                csv.append("No data available for this report type");
        }
        
        return csv.toString().getBytes();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    // PDF Helper Methods (iText)
    private void addReportHeader(Document document, ReportRequestDto request) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        // Title
        Paragraph title = new Paragraph("BGC Events - " + request.getReportType().toString().replace("_", " "))
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        // Date range
        if (request.getStartDate() != null && request.getEndDate() != null) {
            Paragraph dateRange = new Paragraph(
                    String.format("Period: %s to %s",
                            request.getStartDate().format(DATE_ONLY_FORMAT),
                            request.getEndDate().format(DATE_ONLY_FORMAT)))
                    .setFont(normalFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(dateRange);
        }
        
        // Generated date
        Paragraph generated = new Paragraph("Generated: " + LocalDateTime.now().format(DATE_FORMAT))
                .setFont(normalFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(generated);
        
        document.add(new Paragraph("\n"));
    }
    
    private void addReportFooter(Document document) throws IOException {
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        Paragraph footer = new Paragraph()
                .add(new Paragraph("\n"))
                .add(new Paragraph("© " + LocalDateTime.now().getYear() + " BGC Events. All rights reserved.")
                        .setFont(normalFont)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("This is a system-generated report.")
                        .setFont(normalFont)
                        .setFontSize(6)
                        .setTextAlignment(TextAlignment.CENTER));
        
        document.add(footer);
    }
    
    private void addPdfTableHeader(Table table, String... headers) throws IOException {
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(boldFont))
                    .setBackgroundColor(PRIMARY_COLOR)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }
    }
    
    // Excel Helper Methods (Apache POI)
    private Map<String, org.apache.poi.ss.usermodel.CellStyle> createExcelStyles(Workbook workbook) {
        Map<String, org.apache.poi.ss.usermodel.CellStyle> styles = new HashMap<>();
        
        // Create fonts
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        
        Font normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 11);
        
        // Title style
        org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("title", titleStyle);
        
        // Header style
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);
        
        // Info style
        org.apache.poi.ss.usermodel.CellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setFont(normalFont);
        infoStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        infoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("info", infoStyle);
        
        // Success style
        org.apache.poi.ss.usermodel.CellStyle successStyle = workbook.createCellStyle();
        successStyle.setFont(normalFont);
        successStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("success", successStyle);
        
        // Warning style
        org.apache.poi.ss.usermodel.CellStyle warningStyle = workbook.createCellStyle();
        warningStyle.setFont(normalFont);
        warningStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("warning", warningStyle);
        
        // Danger style
        org.apache.poi.ss.usermodel.CellStyle dangerStyle = workbook.createCellStyle();
        dangerStyle.setFont(normalFont);
        dangerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.index);
        dangerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("danger", dangerStyle);
        
        // Neutral style
        org.apache.poi.ss.usermodel.CellStyle neutralStyle = workbook.createCellStyle();
        neutralStyle.setFont(normalFont);
        styles.put("neutral", neutralStyle);
        
        // Status styles
        String[] statuses = {"open", "full", "closed", "draft", "cancelled", "completed", 
                            "confirmed", "pending", "attended", "waitlisted"};
        for (String status : statuses) {
            org.apache.poi.ss.usermodel.CellStyle statusStyle = workbook.createCellStyle();
            statusStyle.setFont(normalFont);
            
            switch (status) {
                case "open":
                case "confirmed":
                case "attended":
                    statusStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    break;
                case "full":
                case "cancelled":
                    statusStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
                    break;
                case "closed":
                case "pending":
                    statusStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                    break;
                default:
                    statusStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            }
            
            statusStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            statusStyle.setBorderTop(BorderStyle.THIN);
            statusStyle.setBorderBottom(BorderStyle.THIN);
            statusStyle.setBorderLeft(BorderStyle.THIN);
            statusStyle.setBorderRight(BorderStyle.THIN);
            styles.put("status_" + status, statusStyle);
        }
        
        return styles;
    }
    
    private void createRegistrationSummarySheet(Workbook workbook, 
                                               Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                               Event event, List<Registration> registrations) {
        Sheet sheet = workbook.createSheet("Summary");
        
        // Title
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Registration Summary - " + event.getTitle());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        // Statistics
        int rowNum = 2;
        
        // Basic stats
        Row stats1Row = sheet.createRow(rowNum++);
        stats1Row.createCell(0).setCellValue("Total Registrations:");
        stats1Row.getCell(0).setCellStyle(styles.get("header"));
        stats1Row.createCell(1).setCellValue(registrations.size());
        
        Row stats2Row = sheet.createRow(rowNum++);
        stats2Row.createCell(0).setCellValue("Confirmed:");
        stats2Row.getCell(0).setCellStyle(styles.get("header"));
        stats2Row.createCell(1).setCellValue(
                registrations.stream().filter(r -> r.getStatus() == Registration.RegistrationStatus.CONFIRMED).count());
        
        Row stats3Row = sheet.createRow(rowNum++);
        stats3Row.createCell(0).setCellValue("Checked In:");
        stats3Row.getCell(0).setCellStyle(styles.get("header"));
        stats3Row.createCell(1).setCellValue(
                registrations.stream().filter(Registration::isCheckedIn).count());
        
        Row stats4Row = sheet.createRow(rowNum++);
        stats4Row.createCell(0).setCellValue("Attendance Rate:");
        stats4Row.getCell(0).setCellStyle(styles.get("header"));
        stats4Row.createCell(1).setCellValue(String.format("%.1f%%", 
                registrations.size() > 0 ? 
                (registrations.stream().filter(Registration::isCheckedIn).count() * 100.0 / registrations.size()) : 0));
        
        rowNum += 2;
        
        // Status breakdown
        Row statusHeaderRow = sheet.createRow(rowNum++);
        statusHeaderRow.createCell(0).setCellValue("Status Breakdown");
        statusHeaderRow.getCell(0).setCellStyle(styles.get("header"));
        
        for (Registration.RegistrationStatus status : Registration.RegistrationStatus.values()) {
            Row statusRow = sheet.createRow(rowNum++);
            statusRow.createCell(0).setCellValue(status.toString());
            long count = registrations.stream().filter(r -> r.getStatus() == status).count();
            statusRow.createCell(1).setCellValue(count);
            statusRow.createCell(2).setCellValue(String.format("%.1f%%", 
                    registrations.size() > 0 ? (count * 100.0 / registrations.size()) : 0));
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void addEventsSummaryPdf(Document document, ReportRequestDto request) throws IOException {
        List<Event> events = eventRepository.findAll();
        
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 1, 1, 1, 1}))
                .useAllAvailableWidth();
        
        addPdfTableHeader(table, "ID", "Event Title", "Date", "Venue", "Cap.", "Reg.", "Checked", "Status");
        
        for (Event event : events) {
            long registrations = registrationRepository.countByEventIdAndDeletedFalse(event.getId());
            long checkedIn = registrationRepository.countCheckedInByEvent(event.getId());
            
            table.addCell(new Cell().add(new Paragraph(String.valueOf(event.getId()))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(event.getTitle())).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(event.getStartDate().format(DATE_ONLY_FORMAT))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(event.getVenue())).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(event.getCapacity() != null ? event.getCapacity() : "∞"))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(registrations))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(checkedIn))).setFont(normalFont));
            
            Cell statusCell = new Cell().add(new Paragraph(event.getStatus().toString())).setFont(normalFont);
            switch (event.getStatus()) {
                case OPEN:
                    statusCell.setFontColor(SUCCESS_COLOR);
                    break;
                case FULL:
                    statusCell.setFontColor(DANGER_COLOR);
                    break;
                case CLOSED:
                    statusCell.setFontColor(WARNING_COLOR);
                    break;
                default:
                    break;
            }
            table.addCell(statusCell);
        }
        
        document.add(table);
    }
    
    private void addRegistrationsDetailedPdf(Document document, ReportRequestDto request) throws IOException {
        List<Registration> registrations;
        
        if (request.getEventIds() != null && !request.getEventIds().isEmpty()) {
            registrations = registrationRepository.findByEventId(request.getEventIds().get(0), Pageable.unpaged()).getContent();
        } else {
            registrations = registrationRepository.findAll();
        }
        
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3, 2, 2, 1, 1}))
                .useAllAvailableWidth();
        
        addPdfTableHeader(table, "#", "Name", "Email", "Event", "Status", "Checked", "Date");
        
        int count = 1;
        for (Registration reg : registrations.stream().limit(100).collect(Collectors.toList())) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(count++))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(reg.getFirstName() + " " + reg.getLastName())).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(reg.getEmail())).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(reg.getEvent().getTitle())).setFont(normalFont));
            
            Cell statusCell = new Cell().add(new Paragraph(reg.getStatus().toString())).setFont(normalFont);
            if (reg.getStatus() == Registration.RegistrationStatus.CONFIRMED) {
                statusCell.setFontColor(SUCCESS_COLOR);
            } else if (reg.getStatus() == Registration.RegistrationStatus.CANCELLED) {
                statusCell.setFontColor(DANGER_COLOR);
            }
            table.addCell(statusCell);
            
            table.addCell(new Cell().add(new Paragraph(reg.isCheckedIn() ? "✓" : "✗")).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(reg.getCreatedAt().format(DATE_ONLY_FORMAT))).setFont(normalFont));
        }
        
        document.add(table);
    }
    
    private void addAttendanceReportPdf(Document document, ReportRequestDto request) throws IOException {
        document.add(new Paragraph("Attendance Report - Coming Soon"));
    }
    
    private void addOrganizerPerformancePdf(Document document, ReportRequestDto request) throws IOException {
        List<User> organizers = userRepository.findByRoleName("ORGANIZER");
        
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2, 2}))
                .useAllAvailableWidth();
        
        addPdfTableHeader(table, "Organizer", "Events", "Registrations", "Attendance", "Rate");
        
        for (User organizer : organizers) {
            long eventsCount = eventRepository.countEventsByOrganizer(organizer.getId());
            Long totalRegs = eventRepository.getTotalRegistrationsByOrganizer(organizer.getId());
            totalRegs = totalRegs != null ? totalRegs : 0L;
            
            // This would need actual attendance calculation
            long attendance = 0;
            
            table.addCell(new Cell().add(new Paragraph(organizer.getFirstName() + " " + organizer.getLastName())).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(eventsCount))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(totalRegs))).setFont(normalFont));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(attendance))).setFont(normalFont));
            
            double rate = totalRegs > 0 ? (attendance * 100.0 / totalRegs) : 0;
            table.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", rate))).setFont(normalFont));
        }
        
        document.add(table);
    }
    
    private void addSystemAuditPdf(Document document, ReportRequestDto request) {
        document.add(new Paragraph("System Audit Report - Coming Soon"));
    }
    
    private void addDefaultReportPdf(Document document, ReportRequestDto request) {
        document.add(new Paragraph("Default Report Content"));
    }
    
    private void createEventsSummarySheet(Workbook workbook, 
                                         Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                         ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Events Summary");
        // Implementation similar to exportEventsSummaryToExcel
    }
    
    private void createRegistrationsDetailedSheet(Workbook workbook, 
                                                 Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                                 ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Registrations");
        // Implementation
    }
    
    private void createAttendanceReportSheet(Workbook workbook, 
                                            Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                            ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Attendance");
        // Implementation
    }
    
    private void createOrganizerPerformanceSheet(Workbook workbook, 
                                                Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                                ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Organizer Performance");
        // Implementation
    }
    
    private void createSystemAuditSheet(Workbook workbook, 
                                       Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                       ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("System Audit");
        // Implementation
    }
    
    private void createDefaultSheet(Workbook workbook, 
                                   Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                   ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Report");
        // Implementation
    }
    
    private void createSummarySheet(Workbook workbook, 
                                   Map<String, org.apache.poi.ss.usermodel.CellStyle> styles, 
                                   ReportRequestDto request) {
        Sheet sheet = workbook.createSheet("Summary");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Report Summary - " + request.getReportType());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum++;
        
        // Report info
        Row typeRow = sheet.createRow(rowNum++);
        typeRow.createCell(0).setCellValue("Report Type:");
        typeRow.getCell(0).setCellStyle(styles.get("header"));
        typeRow.createCell(1).setCellValue(request.getReportType().toString());
        
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Generated:");
        dateRow.getCell(0).setCellStyle(styles.get("header"));
        dateRow.createCell(1).setCellValue(LocalDateTime.now().format(DATE_FORMAT));
        
        if (request.getStartDate() != null) {
            Row startRow = sheet.createRow(rowNum++);
            startRow.createCell(0).setCellValue("Start Date:");
            startRow.getCell(0).setCellStyle(styles.get("header"));
            startRow.createCell(1).setCellValue(request.getStartDate().format(DATE_ONLY_FORMAT));
        }
        
        if (request.getEndDate() != null) {
            Row endRow = sheet.createRow(rowNum++);
            endRow.createCell(0).setCellValue("End Date:");
            endRow.getCell(0).setCellStyle(styles.get("header"));
            endRow.createCell(1).setCellValue(request.getEndDate().format(DATE_ONLY_FORMAT));
        }
        
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void generateEventsSummaryCsv(StringBuilder csv, ReportRequestDto request) {
        List<Event> events = eventRepository.findAll();
        
        csv.append("ID,Title,Start Date,End Date,Venue,City,Capacity,Registrations,Status,Organizer\n");
        
        for (Event event : events) {
            long registrations = registrationRepository.countByEventIdAndDeletedFalse(event.getId());
            
            csv.append(event.getId()).append(",")
               .append(escapeCsv(event.getTitle())).append(",")
               .append(event.getStartDate().format(DATE_ONLY_FORMAT)).append(",")
               .append(event.getEndDate().format(DATE_ONLY_FORMAT)).append(",")
               .append(escapeCsv(event.getVenue())).append(",")
               .append(escapeCsv(event.getCity() != null ? event.getCity() : "")).append(",")
               .append(event.getCapacity() != null ? event.getCapacity() : 0).append(",")
               .append(registrations).append(",")
               .append(event.getStatus()).append(",")
               .append(escapeCsv(event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName()))
               .append("\n");
        }
    }
    
    private void generateRegistrationsDetailedCsv(StringBuilder csv, ReportRequestDto request) {
        List<Registration> registrations = registrationRepository.findAll();
        
        csv.append("ID,Name,Email,Phone,Organization,Event,Status,Checked In,Registered At\n");
        
        for (Registration reg : registrations.stream().limit(1000).collect(Collectors.toList())) {
            csv.append(reg.getId()).append(",")
               .append(escapeCsv(reg.getFirstName() + " " + reg.getLastName())).append(",")
               .append(escapeCsv(reg.getEmail())).append(",")
               .append(escapeCsv(reg.getPhoneNumber() != null ? reg.getPhoneNumber() : "")).append(",")
               .append(escapeCsv(reg.getOrganization() != null ? reg.getOrganization() : "")).append(",")
               .append(escapeCsv(reg.getEvent().getTitle())).append(",")
               .append(reg.getStatus()).append(",")
               .append(reg.isCheckedIn() ? "Yes" : "No").append(",")
               .append(reg.getCreatedAt().format(DATE_FORMAT))
               .append("\n");
        }
    }
    
    private void generateAttendanceReportCsv(StringBuilder csv, ReportRequestDto request) {
        csv.append("Attendance Report Data\n");
        // Implementation
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}