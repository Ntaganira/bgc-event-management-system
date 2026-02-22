package com.bgc.event.dto;

import java.util.Map;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RegistrationStatistics.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Statistics DTO for registration analytics
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStatistics {
    private Long eventId;
    private int totalRegistrations;
    private int confirmedCount;
    private int checkedInCount;
    private int cancelledCount;
    private int waitlistCount;
    private double attendanceRate;
    private double confirmationRate;
    private int availableSpots;
    private Map<String, Long> registrationsByDay;
    private Map<String, Long> registrationsByOrganization;
}