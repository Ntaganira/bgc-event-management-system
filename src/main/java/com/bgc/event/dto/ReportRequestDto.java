package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : ReportRequestDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Report generation request DTO
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private List<Long> eventIds;
    private Long organizerId;
    
    private List<String> includedMetrics;
    private boolean includeCharts;
    private boolean includeRawData;
    
    private String format; // PDF, EXCEL, CSV
    private String title;
    private String description;
    
    public enum ReportType {
        EVENTS_SUMMARY,
        REGISTRATIONS_DETAILED,
        ATTENDANCE_REPORT,
        ORGANIZER_PERFORMANCE,
        SYSTEM_AUDIT,
        CUSTOM
    }
}