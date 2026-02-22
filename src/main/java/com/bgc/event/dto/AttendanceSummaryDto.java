package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : AttendanceSummaryDto.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Summary DTO for attendance analytics
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDto {
    private Long eventId;
    private String eventTitle;
    private int totalRegistrations;
    private int totalCheckedIn;
    private double checkInRate;
    private int remainingCheckIns;
    private List<Map<String, Object>> checkInTrend;
    private Map<String, Long> checkInMethods;
    private String peakCheckInTime;
    private LocalDateTime lastUpdated;
}