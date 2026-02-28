package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : DashboardController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Main dashboard with stats overview
 * </pre>
 */

import com.bgc.event.repository.AttendanceRepository;
import com.bgc.event.service.EventService;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EventService eventService;
    private final UserService userService;
    private final AttendanceRepository attendanceRepository;

    @GetMapping({"/", "/dashboard"})
    @PreAuthorize("hasAuthority('VIEW_DASHBOARD')")
    public String dashboard(Model model) {
        model.addAttribute("totalEvents", eventService.count());
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalAttendance", attendanceRepository.count());
        model.addAttribute("upcomingEvents", eventService.findUpcoming());
        model.addAttribute("recentAttendance", attendanceRepository.findAll()
            .stream().limit(5).toList());
        return "dashboard";
    }
}
