package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : UserStatDto.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : User statistics DTO for analytics
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
public class UserStatDto {
    private Long userId;
    private String name;
    private String email;
    private String role;
    
    private Long eventsCreated;
    private Long totalRegistrations;
    private Long totalAttendees;
    private Double averageAttendanceRate;
    
    private Long lastActiveDays;
    private String status;
    
    public Double getAverageAttendanceRate() {
        if (totalRegistrations == null || totalRegistrations == 0) return 0.0;
        return (totalAttendees * 100.0) / totalRegistrations;
    }
    
    public String getFormattedAttendanceRate() {
        return String.format("%.1f%%", getAverageAttendanceRate());
    }
    
    public boolean isActive() {
        return lastActiveDays != null && lastActiveDays < 7;
    }
}