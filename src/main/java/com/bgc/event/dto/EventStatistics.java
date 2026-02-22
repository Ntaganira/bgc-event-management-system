package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventStatistics.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Statistics DTO for event analytics
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
public class EventStatistics {
    private Long eventId;
    private String eventTitle;
    private int totalRegistrations;
    private int checkedIn;
    private double attendanceRate;
    private int waitlistCount;
    private int availableSpots;
    private boolean isFull;
    private List<Object[]> registrationTrend;
    
    // For charts and graphs
    private Map<String, Long> registrationsByDate;
    private Map<String, Long> registrationsByStatus;
    private Map<String, Long> registrationsByOrganization;
}