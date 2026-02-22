package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventAnalyticsDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Detailed event analytics DTO
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
public class EventAnalyticsDto {
    
    // Basic info
    private Long eventId;
    private String eventTitle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    
    // Registration stats
    private RegistrationStats registrationStats;
    
    // Demographics
    private DemographicsDto demographics;
    
    // Time series
    private List<TimeSeriesPoint> registrationTimeline;
    private List<TimeSeriesPoint> checkInTimeline;
    
    // Comparison
    private ComparisonDto comparison;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationStats {
        private Long totalRegistrations;
        private Long confirmed;
        private Long pending;
        private Long cancelled;
        private Long waitlisted;
        private Long checkedIn;
        private Double checkInRate;
        private Double conversionRate;
        private Integer availableSpots;
        private Long uniqueOrganizations;
        private Map<String, Long> registrationsByStatus;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemographicsDto {
        private Map<String, Long> byOrganization;
        private Map<String, Long> byJobTitle;
        private Map<String, Long> byDietaryRestrictions;
        private Map<String, Long> byRegistrationHour;
        private Map<String, Long> byDeviceType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private String date;
        private Long registrations;
        private Long checkIns;
        private Double cumulativeRegistrations;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonDto {
        private Double vsAverageAttendance;
        private Double vsSimilarEvents;
        private Integer percentileRank;
        private String trend;
    }
}