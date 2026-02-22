package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : AuditSummaryDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : DTO for audit summary dashboard
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSummaryDto {
    private Long totalLogs;
    private Map<String, Long> activityByCategory;
    private List<Map<String, Object>> mostActiveEntities;
    private Map<String, Long> hourlyDistribution;
    private List<AuditLogDto> recentActivities;
}