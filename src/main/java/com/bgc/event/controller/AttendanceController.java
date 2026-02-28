package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AttendanceController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Attendance check-in via QR code or manual code entry
 * </pre>
 */

import com.bgc.event.entity.User;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AttendanceService;
import com.bgc.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
 
    private final AttendanceService attendanceService;
    private final EventService eventService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('MARK_ATTENDANCE')")
    public String attendancePage(Model model) {
        model.addAttribute("events", eventService.findAll());
        return "attendance/index";
    }

    @PostMapping("/scan-qr")
    @PreAuthorize("hasAuthority('MARK_ATTENDANCE')")
    public String scanQR(@RequestParam String qrValue,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        try {
            attendanceService.markByQR(user, qrValue);
            redirectAttributes.addFlashAttribute("successMsg", "Attendance recorded via QR code!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @PostMapping("/mark-code")
    @PreAuthorize("hasAuthority('MARK_ATTENDANCE')")
    public String markByCode(@RequestParam Long eventId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        try {
            attendanceService.markByCode(user, eventId);
            redirectAttributes.addFlashAttribute("successMsg", "Attendance recorded!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAuthority('VIEW_ATTENDANCE')")
    public String viewEventAttendance(@PathVariable Long eventId, Model model) {
        var event = eventService.findById(eventId).orElseThrow();
        model.addAttribute("event", event);
        model.addAttribute("records", attendanceService.findByEvent(event));
        model.addAttribute("count", attendanceService.countByEvent(event));
        return "attendance/event-detail";
    }
}
