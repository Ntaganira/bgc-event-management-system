package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : DashboardController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Dashboard with ECharts data — attendance trend, method split,
 *                top events, events per month, top users
 * </pre>
 */

import com.bgc.event.repository.AttendanceRepository;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EventRepository      eventRepository;
    private final UserService          userService;
    private final AttendanceRepository attendanceRepository;
    private final ObjectMapper         objectMapper;

    @GetMapping({"/", "/dashboard"})
    @PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
    public String dashboard(Model model) {
        LocalDateTime now = LocalDateTime.now();  

        // ── Stat cards ────────────────────────────────────────────────────
        model.addAttribute("totalEvents",     eventRepository.count());
        model.addAttribute("totalUsers",      userService.count());
        model.addAttribute("totalAttendance", attendanceRepository.count());
        model.addAttribute("upcomingCount",   eventRepository.countUpcoming(now));

        // ── Tables ────────────────────────────────────────────────────────
        model.addAttribute("upcomingEvents",   eventRepository.findUpcoming(now).stream().limit(5).toList());
        model.addAttribute("recentAttendance", attendanceRepository.findTop5ByOrderByAttendanceTimeDesc());

        // ── Chart 1: Daily check-ins (last 7 days) — Line chart ──────────
        try {
            List<Object[]> daily = attendanceRepository.dailyCheckInsLast7Days();
            List<String> dailyLabels  = new ArrayList<>();
            List<Long>   dailyValues  = new ArrayList<>();
            for (Object[] row : daily) {
                dailyLabels.add(String.valueOf(row[0]));
                dailyValues.add(((Number) row[1]).longValue());
            }
            model.addAttribute("chartDailyLabels", objectMapper.writeValueAsString(dailyLabels));
            model.addAttribute("chartDailyValues", objectMapper.writeValueAsString(dailyValues));
        } catch (Exception e) {
            log.warn("Daily chart data error: {}", e.getMessage());
            model.addAttribute("chartDailyLabels", "[]");
            model.addAttribute("chartDailyValues", "[]");
        }

        // ── Chart 2: Attendance per event — Bar chart ────────────────────
        try {
            List<Object[]> perEvent = attendanceRepository.countPerEvent();
            List<String> eventNames  = new ArrayList<>();
            List<Long>   eventCounts = new ArrayList<>();
            for (Object[] row : perEvent.stream().limit(8).toList()) {
                eventNames.add(String.valueOf(row[0]));
                eventCounts.add(((Number) row[1]).longValue());
            }
            model.addAttribute("chartEventLabels", objectMapper.writeValueAsString(eventNames));
            model.addAttribute("chartEventValues", objectMapper.writeValueAsString(eventCounts));
        } catch (Exception e) {
            log.warn("Per-event chart data error: {}", e.getMessage());
            model.addAttribute("chartEventLabels", "[]");
            model.addAttribute("chartEventValues", "[]");
        }

        // ── Chart 3: QR vs CODE — Donut chart ────────────────────────────
        try {
            List<Object[]> methods = attendanceRepository.countByMethod();
            List<Map<String,Object>> methodData = new ArrayList<>();
            for (Object[] row : methods) {
                Map<String,Object> item = new LinkedHashMap<>();
                item.put("name",  String.valueOf(row[0]));
                item.put("value", ((Number) row[1]).longValue());
                methodData.add(item);
            }
            model.addAttribute("chartMethodData", objectMapper.writeValueAsString(methodData));
        } catch (Exception e) {
            log.warn("Method chart data error: {}", e.getMessage());
            model.addAttribute("chartMethodData", "[]");
        }

        // ── Chart 4: Events per month (current year) — Bar chart ─────────
        try {
            List<Object[]> monthly = eventRepository.eventsPerMonth();
            // Fill all 12 months with zeros
            String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            long[] monthValues  = new long[12];
            for (Object[] row : monthly) {
                int m = ((Number) row[0]).intValue();
                monthValues[m - 1] = ((Number) row[2]).longValue();
            }
            List<Long> mv = new ArrayList<>();
            for (long v : monthValues) mv.add(v);
            model.addAttribute("chartMonthLabels", objectMapper.writeValueAsString(List.of(monthNames)));
            model.addAttribute("chartMonthValues", objectMapper.writeValueAsString(mv));
        } catch (Exception e) {
            log.warn("Monthly chart data error: {}", e.getMessage());
            model.addAttribute("chartMonthLabels", "[]");
            model.addAttribute("chartMonthValues", "[]");
        }

        // ── Chart 5: Top active users — Horizontal bar ───────────────────
        try {
            List<Object[]> topUsers = attendanceRepository.topActiveUsers();
            List<String> userNames   = new ArrayList<>();
            List<Long>   userCounts  = new ArrayList<>();
            for (Object[] row : topUsers) {
                userNames.add(String.valueOf(row[0]));
                userCounts.add(((Number) row[1]).longValue());
            }
            model.addAttribute("chartUserLabels", objectMapper.writeValueAsString(userNames));
            model.addAttribute("chartUserValues", objectMapper.writeValueAsString(userCounts));
        } catch (Exception e) {
            log.warn("Top users chart data error: {}", e.getMessage());
            model.addAttribute("chartUserLabels", "[]");
            model.addAttribute("chartUserValues", "[]");
        }

        return "dashboard";
    }
}
