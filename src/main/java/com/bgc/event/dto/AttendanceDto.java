package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : AttendanceDto.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : DTO for attendance records
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long registrationId;
    private String attendeeName;
    private String email;
    private LocalDateTime checkedInAt;
    private String checkedInBy;
    private String checkInMethod;
    private String ipAddress;
    private String deviceInfo;
    private String notes;
}