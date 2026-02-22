package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : ReportExportService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service for exporting reports to various formats
 * </pre>
 */

import com.bgc.event.dto.ReportRequestDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;

import java.util.List;

public interface ReportExportService {
    
    byte[] generatePdfReport(ReportRequestDto request);
    
    byte[] generateExcelReport(ReportRequestDto request);
    
    byte[] generateCsvReport(ReportRequestDto request);
    
    byte[] exportRegistrationsToPdf(Event event, List<Registration> registrations);
    
    byte[] exportRegistrationsToExcel(Event event, List<Registration> registrations);
    
    byte[] exportEventsSummaryToExcel(List<Event> events);
}