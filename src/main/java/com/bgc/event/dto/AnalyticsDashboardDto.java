package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : AnalyticsDashboardDto.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Dashboard analytics DTO for main overview
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
public class AnalyticsDashboardDto {
    
    // Overview stats
    private Long totalEvents;
    private Long activeEvents;
    private Long upcomingEvents;
    private Long completedEvents;
    private Long totalRegistrations;
    private Long totalAttendees;
    private Double averageAttendanceRate;
    
    // Growth rates
    private Double eventGrowthRate;
    private Double registrationGrowthRate;
    private Double clientGrowthRate;
    private Double userGrowthRate;
    
    // Time-based stats
    private Map<String, Long> registrationsLast7Days;
    private Map<String, Long> eventsByMonth;
    private Map<String, Long> usersByMonth;
    
    // Top performers
    private List<EventStatDto> topEventsByRegistrations;
    private List<EventStatDto> topEventsByAttendance;
    private List<UserStatDto> topOrganizers;
    
    // Organizer stats
    private Map<String, Object> organizerStats;
    private Long uniqueOrganizations;
    
    // System health
    private SystemHealthDto systemHealth;
    
    // Timestamps
    private LocalDateTime lastUpdated;
    private LocalDateTime dataFrom;
    private LocalDateTime dataTo;
    
    // ==================== GETTERS FOR GROWTH RATES ====================
    
    public Double getEventGrowthRate() {
        return eventGrowthRate != null ? eventGrowthRate : 0.0;
    }
    
    public Double getRegistrationGrowthRate() {
        return registrationGrowthRate != null ? registrationGrowthRate : 0.0;
    }
    
    public Double getClientGrowthRate() {
        return clientGrowthRate != null ? clientGrowthRate : 0.0;
    }
    
    public Double getUserGrowthRate() {
        return userGrowthRate != null ? userGrowthRate : 0.0;
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    public Long getUniqueOrganizations() {
        return uniqueOrganizations != null ? uniqueOrganizations : 0L;
    }
    
    public String getFormattedEventGrowthRate() {
        return formatGrowthRate(eventGrowthRate);
    }
    
    public String getFormattedRegistrationGrowthRate() {
        return formatGrowthRate(registrationGrowthRate);
    }
    
    public String getFormattedClientGrowthRate() {
        return formatGrowthRate(clientGrowthRate);
    }
    
    public String getFormattedUserGrowthRate() {
        return formatGrowthRate(userGrowthRate);
    }
    
    private String formatGrowthRate(Double rate) {
        if (rate == null) return "0%";
        String sign = rate > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, rate);
    }
    
    // ==================== STATISTICS CALCULATIONS ====================
    
    public Double getAverageEventRegistrations() {
        if (totalEvents == null || totalEvents == 0 || totalRegistrations == null) return 0.0;
        return (double) totalRegistrations / totalEvents;
    }
    
    public Double getAverageEventAttendance() {
        if (totalEvents == null || totalEvents == 0 || totalAttendees == null) return 0.0;
        return (double) totalAttendees / totalEvents;
    }
    
    public Long getPendingRegistrations() {
        return totalRegistrations != null && totalAttendees != null ? 
               totalRegistrations - totalAttendees : 0L;
    }
    
    public Double getOverallCheckInRate() {
        if (totalRegistrations == null || totalRegistrations == 0) return 0.0;
        return (totalAttendees * 100.0) / totalRegistrations;
    }
    
    // ==================== BUILDER EXTENSION ====================
    
    public static class AnalyticsDashboardDtoBuilder {
        private Double eventGrowthRate;
        private Double registrationGrowthRate;
        private Double clientGrowthRate;
        private Double userGrowthRate;
        private Long uniqueOrganizations;
        
        public AnalyticsDashboardDtoBuilder eventGrowthRate(Double rate) {
            this.eventGrowthRate = rate;
            return this;
        }
        
        public AnalyticsDashboardDtoBuilder registrationGrowthRate(Double rate) {
            this.registrationGrowthRate = rate;
            return this;
        }
        
        public AnalyticsDashboardDtoBuilder clientGrowthRate(Double rate) {
            this.clientGrowthRate = rate;
            return this;
        }
        
        public AnalyticsDashboardDtoBuilder userGrowthRate(Double rate) {
            this.userGrowthRate = rate;
            return this;
        }
        
        public AnalyticsDashboardDtoBuilder uniqueOrganizations(Long count) {
            this.uniqueOrganizations = count;
            return this;
        }
    }
}