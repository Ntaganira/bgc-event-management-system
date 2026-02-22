package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : AnalyticsServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Analytics and Reporting Service
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import com.bgc.event.exception.EventException;
import com.bgc.event.repository.AnalyticsRepository;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AnalyticsService;
import com.bgc.event.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private final AnalyticsRepository analyticsRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final ReportExportService reportExportService;
    
    // private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    @Cacheable(value = "dashboard-analytics", key = "'dashboard'")
    public AnalyticsDashboardDto getDashboardAnalytics() {
        log.info("Generating dashboard analytics");
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        // Get overview stats
        AnalyticsDashboardDto dashboard = AnalyticsDashboardDto.builder()
                .totalEvents(analyticsRepository.getTotalEvents())
                .activeEvents(analyticsRepository.getActiveEvents())
                .upcomingEvents(analyticsRepository.getUpcomingEvents())
                .completedEvents(analyticsRepository.getCompletedEvents())
                .totalRegistrations(analyticsRepository.getTotalRegistrations())
                .totalAttendees(analyticsRepository.getTotalAttendees())
                .averageAttendanceRate(calculateAverageAttendanceRate())
                .build();
        
        // Get registration trends for last 7 days
        dashboard.setRegistrationsLast7Days(getRegistrationTrendMap(sevenDaysAgo));
        
        // Get events by month
        dashboard.setEventsByMonth(getEventsByMonth());
        
        // Get top events
        dashboard.setTopEventsByRegistrations(getTopEventStats("registrations", 5));
        dashboard.setTopEventsByAttendance(getTopEventStats("attendance", 5));
        
        // Get organizer stats
        dashboard.setOrganizerStats(getOrganizerStats());
        
        // Get system health
        dashboard.setSystemHealth(getSystemHealth());
        
        return dashboard;
    }
    
    @Override
    @Cacheable(value = "event-analytics", key = "#eventId")
    public EventAnalyticsDto getEventAnalytics(Long eventId) throws EventException {
        log.info("Generating analytics for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found with ID: " + eventId));
        
        // Build registration stats
        EventAnalyticsDto.RegistrationStats regStats = buildRegistrationStats(eventId);
        
        // Build demographics
        EventAnalyticsDto.DemographicsDto demographics = buildDemographics(eventId);
        
        // Build time series
        List<EventAnalyticsDto.TimeSeriesPoint> timeline = buildTimeSeries(eventId);
        
        // Build comparison
        EventAnalyticsDto.ComparisonDto comparison = buildComparison(eventId);
        
        return EventAnalyticsDto.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus().toString())
                .registrationStats(regStats)
                .demographics(demographics)
                .registrationTimeline(timeline)
                .checkInTimeline(buildCheckInTimeSeries(eventId))
                .comparison(comparison)
                .build();
    }
    
    @Override
    public Page<EventStatDto> getAllEventsAnalytics(Pageable pageable) {
        log.debug("Fetching analytics for all events, page: {}", pageable);
        
        Page<Event> events = eventRepository.findByDeletedFalse(pageable);
        List<EventStatDto> stats = events.getContent().stream()
                .map(this::mapToEventStat)
                .collect(Collectors.toList());
        
        return new PageImpl<>(stats, pageable, events.getTotalElements());
    }
    
    @Override
    public Page<EventStatDto> getOrganizerEventsAnalytics(Long organizerId, Pageable pageable) {
        log.debug("Fetching analytics for organizer ID: {}, page: {}", organizerId, pageable);
        
        return userRepository.findById(organizerId)
                .map(organizer -> {
                    Page<Event> events = eventRepository.findByOrganizer(organizer, pageable);
                    List<EventStatDto> stats = events.getContent().stream()
                            .map(this::mapToEventStat)
                            .collect(Collectors.toList());
                    return new PageImpl<>(stats, pageable, events.getTotalElements());
                }).orElse(null);
    }
    
    @Override
    public List<TimeSeriesData> getRegistrationTrends(Long eventId, LocalDateTime startDate, 
                                                      LocalDateTime endDate, String interval) {
        log.debug("Getting registration trends for event: {}, interval: {}", eventId, interval);
        
        List<Object[]> results;
        if (eventId != null) {
            results = analyticsRepository.getDailyRegistrations(eventId);
        } else {
            results = analyticsRepository.getRegistrationTrend(startDate != null ? startDate : 
                                                              LocalDateTime.now().minusMonths(1));
        }
        
        return results.stream()
                .map(result -> TimeSeriesData.builder()
                        .date(result[0].toString())
                        .value(((Number) result[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<EventStatDto> getTopEvents(String metric, int limit) {
        return getTopEventStats(metric, limit);
    }
    
    @Override
    public byte[] generateReport(ReportRequestDto request) throws EventException {
        log.info("Generating report of type: {}, format: {}", request.getReportType(), request.getFormat());
        
        switch (request.getFormat().toUpperCase()) {
            case "PDF":
                return reportExportService.generatePdfReport(request);
            case "EXCEL":
                return reportExportService.generateExcelReport(request);
            case "CSV":
                return reportExportService.generateCsvReport(request);
            default:
                throw new EventException("Unsupported report format: " + request.getFormat());
        }
    }
    
    @Override
    public byte[] exportEventRegistrationsToPdf(Long eventId) throws EventException {
        log.info("Exporting event registrations to PDF for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        
        List<Registration> registrations = registrationRepository.findByEventId(eventId, Pageable.unpaged())
                .getContent();
        
        return reportExportService.exportRegistrationsToPdf(event, registrations);
    }
    
    @Override
    public byte[] exportEventRegistrationsToExcel(Long eventId) throws EventException {
        log.info("Exporting event registrations to Excel for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        
        List<Registration> registrations = registrationRepository.findByEventId(eventId, Pageable.unpaged())
                .getContent();
        
        return reportExportService.exportRegistrationsToExcel(event, registrations);
    }
    
    @Override
    public byte[] exportAllEventsSummaryToExcel() {
        log.info("Exporting all events summary to Excel");
        
        List<Event> events = eventRepository.findAll();
        return reportExportService.exportEventsSummaryToExcel(events);
    }
    
    @Override
    public SystemHealthDto getSystemHealth() {
        log.debug("Getting system health metrics");
        
        return SystemHealthDto.builder()
                .databaseStatus(checkDatabaseHealth())
                .redisStatus(checkRedisHealth())
                .totalUsers(userRepository.count())
                .totalEvents(eventRepository.count())
                .totalRegistrations(registrationRepository.count())
                .averageResponseTime(calculateAverageResponseTime())
                .uptime(getSystemUptime())
                .memoryUsage(getMemoryUsage())
                .build();
    }
    
    @Override
    public Page<AuditLogDto> getAuditLogs(LocalDateTime startDate, LocalDateTime endDate,
                                          String entityType, Pageable pageable) {
        // Implementation would query audit_logs table
        // This is a placeholder
        return Page.empty();
    }
    
    // Private helper methods
    
    private Double calculateAverageAttendanceRate() {
        Long totalRegistrations = analyticsRepository.getTotalRegistrations();
        Long totalAttendees = analyticsRepository.getTotalAttendees();
        
        if (totalRegistrations == null || totalRegistrations == 0) return 0.0;
        return (totalAttendees * 100.0) / totalRegistrations;
    }
    
    private Map<String, Long> getRegistrationTrendMap(LocalDateTime startDate) {
        List<Object[]> trend = analyticsRepository.getRegistrationTrend(startDate);
        
        return trend.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, Long> getEventsByMonth() {
        // This would need a custom query
        // Placeholder implementation
        Map<String, Long> eventsByMonth = new LinkedHashMap<>();
        eventsByMonth.put("2026-01", 5L);
        eventsByMonth.put("2026-02", 8L);
        eventsByMonth.put("2026-03", 12L);
        return eventsByMonth;
    }
    
    private List<EventStatDto> getTopEventStats(String metric, int limit) {
        List<Map<String, Object>> results;
        
        if ("registrations".equals(metric)) {
            results = analyticsRepository.getTopEventsByRegistrations();
        } else {
            results = analyticsRepository.getTopEventsByAttendance();
        }
        
        return results.stream()
                .limit(limit)
                .map(this::mapToEventStat)
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> getOrganizerStats() {
        List<Object[]> results = analyticsRepository.getOrganizerPerformance();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrganizers", results.size());
        stats.put("topOrganizers", results.stream()
                .limit(3)
                .map(arr -> Map.of(
                        "name", arr[1] + " " + arr[2],
                        "events", arr[3],
                        "totalRegistrations", arr[4]
                ))
                .collect(Collectors.toList()));
        
        return stats;
    }
    
    private EventAnalyticsDto.RegistrationStats buildRegistrationStats(Long eventId) {
        Long total = analyticsRepository.countRegistrationsByStatus(eventId, null);
        
        return EventAnalyticsDto.RegistrationStats.builder()
                .totalRegistrations(total)
                .confirmed(analyticsRepository.countRegistrationsByStatus(eventId, "CONFIRMED"))
                .pending(analyticsRepository.countRegistrationsByStatus(eventId, "PENDING"))
                .cancelled(analyticsRepository.countRegistrationsByStatus(eventId, "CANCELLED"))
                .waitlisted(analyticsRepository.countRegistrationsByStatus(eventId, "WAITLISTED"))
                .checkedIn(analyticsRepository.countRegistrationsByStatus(eventId, "ATTENDED"))
                .checkInRate(calculateCheckInRate(eventId))
                .uniqueOrganizations(countUniqueOrganizations(eventId))
                .build();
    }
    
    private Double calculateCheckInRate(Long eventId) {
        Long total = analyticsRepository.countRegistrationsByStatus(eventId, null);
        Long checkedIn = analyticsRepository.countRegistrationsByStatus(eventId, "ATTENDED");
        
        if (total == null || total == 0) return 0.0;
        return (checkedIn * 100.0) / total;
    }
    
    private Long countUniqueOrganizations(Long eventId) {
        List<Object[]> orgs = analyticsRepository.getRegistrationsByOrganization(eventId);
        return (long) orgs.size();
    }
    
    private EventAnalyticsDto.DemographicsDto buildDemographics(Long eventId) {
        return EventAnalyticsDto.DemographicsDto.builder()
                .byOrganization(convertToMap(analyticsRepository.getRegistrationsByOrganization(eventId)))
                .byJobTitle(convertToMap(analyticsRepository.getRegistrationsByJobTitle(eventId)))
                .byRegistrationHour(convertHourDistribution(
                        analyticsRepository.getRegistrationHourDistribution(eventId)))
                .build();
    }
    
    private Map<String, Long> convertToMap(List<Object[]> data) {
        return data.stream()
                .limit(10) // Top 10
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }
    
    private Map<String, Long> convertHourDistribution(List<Object[]> data) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            distribution.put(String.format("%02d:00", hour), 0L);
        }
        
        data.forEach(arr -> {
            String hour = String.format("%02d:00", ((Number) arr[0]).intValue());
            distribution.put(hour, ((Number) arr[1]).longValue());
        });
        
        return distribution;
    }
    
    private List<EventAnalyticsDto.TimeSeriesPoint> buildTimeSeries(Long eventId) {
        List<Object[]> dailyRegs = analyticsRepository.getDailyRegistrations(eventId);
        long cumulative = 0;
        
        List<EventAnalyticsDto.TimeSeriesPoint> timeline = new ArrayList<>();
        
        for (Object[] data : dailyRegs) {
            cumulative += ((Number) data[1]).longValue();
            
            timeline.add(EventAnalyticsDto.TimeSeriesPoint.builder()
                    .date(data[0].toString())
                    .registrations(((Number) data[1]).longValue())
                    .cumulativeRegistrations((double) cumulative)
                    .build());
        }
        
        return timeline;
    }
    
    private List<EventAnalyticsDto.TimeSeriesPoint> buildCheckInTimeSeries(Long eventId) {
        List<Object[]> dailyCheckIns = analyticsRepository.getDailyCheckIns(eventId);
        
        return dailyCheckIns.stream()
                .map(data -> EventAnalyticsDto.TimeSeriesPoint.builder()
                        .date(data[0].toString())
                        .checkIns(((Number) data[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    private EventAnalyticsDto.ComparisonDto buildComparison(Long eventId) {
        Double avgRate = analyticsRepository.getAverageAttendanceRateForSimilarEvents(eventId);
        
        Event event = eventRepository.findById(eventId).orElse(null);
        Double thisEventRate = null;
        
        if (event != null && event.getCurrentRegistrations() > 0) {
            Long checkedIn = analyticsRepository.countRegistrationsByStatus(eventId, "ATTENDED");
            thisEventRate = (checkedIn * 100.0) / event.getCurrentRegistrations();
        }
        
        return EventAnalyticsDto.ComparisonDto.builder()
                .vsAverageAttendance(avgRate)
                .trend(determineTrend(thisEventRate, avgRate))
                .build();
    }
    
    private String determineTrend(Double current, Double average) {
        if (current == null || average == null) return "UNKNOWN";
        if (current > average) return "ABOVE_AVERAGE";
        if (current < average) return "BELOW_AVERAGE";
        return "AVERAGE";
    }
    
    private EventStatDto mapToEventStat(Event event) {
        Long registrations = registrationRepository.countByEventId(event.getId());
        Long checkedIn = registrationRepository.countCheckedInByEvent(event.getId());
        Long waitlist = (long) registrationRepository.countWaitlistByEvent(event.getId());
        
        return EventStatDto.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .organizerName(event.getOrganizer().getFirstName() + " " + event.getOrganizer().getLastName())
                .eventDate(event.getStartDate())
                .registrations(registrations)
                .checkedIn(checkedIn)
                .attendanceRate(registrations > 0 ? (checkedIn * 100.0) / registrations : 0.0)
                .capacity(event.getCapacity())
                .status(event.getStatus().toString())
                .waitlistCount(waitlist)
                .build();
    }
    
    private EventStatDto mapToEventStat(Map<String, Object> data) {
        return EventStatDto.builder()
                .eventId((Long) data.get("eventId"))
                .eventTitle((String) data.get("title"))
                .registrations((Long) data.get("registrations"))
                .checkedIn((Long) data.get("checkedIn"))
                .build();
    }
    
    private boolean checkDatabaseHealth() {
        try {
            analyticsRepository.getTotalEvents();
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }
    
    private boolean checkRedisHealth() {
        // Implement Redis health check
        return true;
    }
    
    private Double calculateAverageResponseTime() {
        // This would come from monitoring system
        return 0.45; // 450ms average
    }
    
    private String getSystemUptime() {
        // This would come from management beans
        return "15 days, 7 hours";
    }
    
    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "total", runtime.totalMemory() / (1024 * 1024),
                "free", runtime.freeMemory() / (1024 * 1024),
                "used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "max", runtime.maxMemory() / (1024 * 1024)
        );
    }
}