package com.bgc.event.dto;

import java.time.LocalDateTime;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventStatDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Event statistics DTO for reporting
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
public class EventStatDto {
    private Long eventId;
    private String eventTitle;
    private String organizerName;
    private LocalDateTime eventDate;
    private Long registrations;
    private Long checkedIn;
    @Builder.Default
    private Double attendanceRate = 0d;
    private Integer capacity;
    private String status;
    private Long waitlistCount;
    
    public Double getAttendanceRate() {
        if (registrations == null || registrations == 0) return 0.0;
        return (checkedIn * 100.0) / registrations;
    }
    
    public Integer getAvailableSpots() {
        if (capacity == null || registrations == null) return 0;
        return capacity - registrations.intValue();
    }
}