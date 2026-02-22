package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : SystemHealthDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : System health metrics DTO
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthDto {
    private boolean databaseStatus;
    private boolean redisStatus;
    private Long totalUsers;
    private Long totalEvents;
    private Long totalRegistrations;
    private Double averageResponseTime;
    private String uptime;
    private Map<String, Object> memoryUsage;
    private Integer activeSessions;
    private Integer apiCallsToday;
}