package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : DashboardController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Dashboard controller with real data binding
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final AnalyticsService analyticsService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final AuditService auditService;
    private final UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("Loading dashboard view");
        
        // Add current date for greeting
        model.addAttribute("currentDate", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        
        return "dashboard";
    }
    
    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        
        AnalyticsDashboardDto analytics = analyticsService.getDashboardAnalytics();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", userService.countActiveUsers());
        stats.put("employeeTrend", calculateTrend(userService.getUserGrowthRate()));
        
        stats.put("totalProjects", analytics.getTotalEvents());
        stats.put("projectsTrend", analytics.getEventGrowthRate());
        
        stats.put("totalClients", analytics.getUniqueOrganizations());
        stats.put("clientsTrend", analytics.getClientGrowthRate());
        
        stats.put("totalRegistrations", analytics.getTotalRegistrations());
        stats.put("registrationsTrend", analytics.getRegistrationGrowthRate());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/api/dashboard/task-stats")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTaskStatistics() {
        log.info("Fetching task statistics");
        
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        // Get real registration status distribution
        Map<String, Long> statusCounts = registrationService.getRegistrationStatusDistribution();
        
        tasks.add(createTaskStat("Completed", 
            statusCounts.getOrDefault("ATTENDED", 0L), 
            statusCounts.getOrDefault("CONFIRMED", 0L) + 
            statusCounts.getOrDefault("ATTENDED", 0L),
            "success"));
        
        tasks.add(createTaskStat("On Hold",
            statusCounts.getOrDefault("PENDING", 0L),
            statusCounts.values().stream().mapToLong(Long::longValue).sum(),
            "warning"));
        
        tasks.add(createTaskStat("In Progress",
            statusCounts.getOrDefault("CONFIRMED", 0L),
            statusCounts.values().stream().mapToLong(Long::longValue).sum(),
            "primary"));
        
        tasks.add(createTaskStat("Rejected",
            statusCounts.getOrDefault("CANCELLED", 0L),
            statusCounts.values().stream().mapToLong(Long::longValue).sum(),
            "danger"));
        
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/api/dashboard/absent-members")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAbsentMembers() {
        log.info("Fetching absent members");
        
        // Get real absent/organizer data
        List<Map<String, Object>> members = userService.getRecentInactiveUsers(7)
            .stream()
            .map(user -> {
                Map<String, Object> member = new HashMap<>();
                member.put("initials", getInitials(user.getFirstName(), user.getLastName()));
                member.put("name", user.getFirstName() + " " + user.getLastName());
                member.put("date", formatAbsentDate(user.getLastLoginAt()));
                member.put("status", getAbsentStatus(user.getLastLoginAt()));
                return member;
            })
            .limit(4)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/api/dashboard/revenue")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueData() {
        log.info("Fetching revenue data");
        
        Map<String, Object> revenue = new HashMap<>();
        
        // Get real registration data by month
        List<Object[]> monthlyData = registrationService.getMonthlyRegistrationStats(
            LocalDateTime.now().minusMonths(12),
            LocalDateTime.now()
        );
        
        List<String> labels = new ArrayList<>();
        List<Long> approvedData = new ArrayList<>();
        List<Long> pendingData = new ArrayList<>();
        
        for (Object[] data : monthlyData) {
            labels.add((String) data[0]); // Month
            approvedData.add((Long) data[1]); // Approved (confirmed + attended)
            pendingData.add((Long) data[2]); // Pending
        }
        
        revenue.put("labels", labels);
        revenue.put("approved", approvedData);
        revenue.put("pending", pendingData);
        
        return ResponseEntity.ok(revenue);
    }
    
    @GetMapping("/api/dashboard/recent-activities")
    @ResponseBody
    public ResponseEntity<List<AuditLogDto>> getRecentActivities() {
        log.info("Fetching recent activities");
        
        Page<AuditLogDto> activities = auditService.searchAuditLogs(
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now(),
            null, null, null, null,
            Pageable.ofSize(10)
        );
        
        return ResponseEntity.ok(activities.getContent());
    }
    
    private Map<String, Object> createTaskStat(String label, Long value, Long total, String color) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("label", label);
        stat.put("value", value);
        stat.put("percentage", total > 0 ? Math.round((value * 100.0) / total) : 0);
        stat.put("color", color);
        return stat;
    }
    
    private String calculateTrend(double growthRate) {
        if (growthRate > 0) {
            return String.format("+%.2f%%", growthRate);
        } else if (growthRate < 0) {
            return String.format("%.2f%%", growthRate);
        } else {
            return "0%";
        }
    }
    
    private String getInitials(String firstName, String lastName) {
        return (firstName != null ? firstName.substring(0, 1) : "") +
               (lastName != null ? lastName.substring(0, 1) : "");
    }
    
    private String formatAbsentDate(LocalDateTime lastLogin) {
        if (lastLogin == null) return "Never logged in";
        
        long days = java.time.Duration.between(lastLogin, LocalDateTime.now()).toDays();
        
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " weeks ago";
        return (days / 30) + " months ago";
    }
    
    private String getAbsentStatus(LocalDateTime lastLogin) {
        if (lastLogin == null) return "inactive";
        
        long days = java.time.Duration.between(lastLogin, LocalDateTime.now()).toDays();
        
        if (days == 0) return "today";
        if (days == 1) return "yesterday";
        if (days < 3) return "recent";
        if (days < 7) return "this-week";
        return "absent";
    }
}