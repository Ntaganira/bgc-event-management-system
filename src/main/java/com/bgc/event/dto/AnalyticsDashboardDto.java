package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : AnalyticsDashboardDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Dashboard analytics DTO for main overview
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
public class AnalyticsDashboardDto {
    
    // Overview stats
    private Long totalEvents;
    private Long activeEvents;
    private Long upcomingEvents;
    private Long completedEvents;
    private Long totalRegistrations;
    private Long totalAttendees;
    private Double averageAttendanceRate;
    
    // Time-based stats
    private Map<String, Long> registrationsLast7Days;
    private Map<String, Long> eventsByMonth;
    
    // Top performers
    private List<EventStatDto> topEventsByRegistrations;
    private List<EventStatDto> topEventsByAttendance;
    
    // Organizer stats
    private Map<String, Object> organizerStats;
    
    // System health
    private SystemHealthDto systemHealth;
}