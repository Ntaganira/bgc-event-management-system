package com.bgc.event.controller;

/**
 * <pre>
 * - Project : BGC EVENT
 * - Package : com.bgc.event.controller
 * - File    : AgendaController.java
 * - Date    : 2026-02-27
 * - Author  : NTAGANIRA Heritier
 * - Desc    : Public agenda — Daily / Weekly / Monthly views.
 *             List + Calendar toggle. No authentication required.
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final EventRepository eventRepository;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ── Main agenda page ──────────────────────────────────────────────
    @GetMapping
    public String agenda(
            @RequestParam(defaultValue = "weekly")  String   view,   // daily | weekly | monthly
            @RequestParam(defaultValue = "list")    String   display, // list | calendar
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate today = LocalDate.now();
        if (date == null) date = today;

        // ── Compute window based on view ──────────────────────────────
        LocalDate windowStart;
        LocalDate windowEnd;
        LocalDate prevDate;
        LocalDate nextDate;

        switch (view) {
            case "daily" -> {
                windowStart = date;
                windowEnd   = date;
                prevDate    = date.minusDays(1);
                nextDate    = date.plusDays(1);
            }
            case "monthly" -> {
                windowStart = date.withDayOfMonth(1);
                windowEnd   = date.withDayOfMonth(date.lengthOfMonth());
                prevDate    = date.minusMonths(1).withDayOfMonth(1);
                nextDate    = date.plusMonths(1).withDayOfMonth(1);
            }
            default -> { // weekly
                WeekFields wf = WeekFields.of(Locale.FRANCE); // Mon–Sun
                windowStart = date.with(wf.dayOfWeek(), 1);
                windowEnd   = windowStart.plusDays(6);
                prevDate    = windowStart.minusWeeks(1);
                nextDate    = windowStart.plusWeeks(1);
            }
        }

        // ── Load events in window ─────────────────────────────────────
        LocalDateTime from = windowStart.atStartOfDay();
        LocalDateTime to   = windowEnd.atTime(23, 59, 59);
        List<Event> events = eventRepository.findForReport(from, to);

        // ── Group by day (for list view) ──────────────────────────────
        Map<LocalDate, List<Event>> byDay = events.stream()
            .collect(Collectors.groupingBy(
                e -> e.getStartDateTime().toLocalDate(),
                TreeMap::new,
                Collectors.toList()
            ));

        // ── Build calendar grid for monthly view ──────────────────────
        List<List<LocalDate>> calendarGrid = buildMonthGrid(date);

        // ── Calendar JSON for FullCalendar ────────────────────────────
        List<Map<String, String>> calEvents = events.stream()
            .map(e -> Map.of(
                "id",    String.valueOf(e.getId()),
                "title", e.getTitle(),
                "start", e.getStartDateTime().format(ISO_FMT),
                "end",   e.getEndDateTime().format(ISO_FMT),
                "url",   "/events/" + e.getId(),
                "location", e.getLocation() != null ? e.getLocation() : ""
            ))
            .collect(Collectors.toList());

        // ── Model ─────────────────────────────────────────────────────
        model.addAttribute("view",         view);
        model.addAttribute("display",      display);
        model.addAttribute("date",         date);
        model.addAttribute("today",        today);
        model.addAttribute("windowStart",  windowStart);
        model.addAttribute("windowEnd",    windowEnd);
        model.addAttribute("prevDate",     prevDate);
        model.addAttribute("nextDate",     nextDate);
        model.addAttribute("events",       events);
        model.addAttribute("byDay",        byDay);
        model.addAttribute("calendarGrid", calendarGrid);
        model.addAttribute("calEvents",    calEvents);
        model.addAttribute("totalCount",   events.size());

        return "agenda/index";
    }

    // ── Build a 6-row calendar grid for monthly view ──────────────────
    private List<List<LocalDate>> buildMonthGrid(LocalDate date) {
        WeekFields wf  = WeekFields.of(Locale.FRANCE);
        LocalDate  first = date.withDayOfMonth(1);
        LocalDate  start = first.with(wf.dayOfWeek(), 1);

        List<List<LocalDate>> grid = new ArrayList<>();
        LocalDate cursor = start;
        for (int row = 0; row < 6; row++) {
            List<LocalDate> week = new ArrayList<>();
            for (int col = 0; col < 7; col++) {
                week.add(cursor);
                cursor = cursor.plusDays(1);
            }
            grid.add(week);
        }
        return grid;
    }
}
