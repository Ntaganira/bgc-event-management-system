package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : EventPageController.java
 * - Date       : 2026. 02. 24.
 * - User       : NTAGANIRA H.
 * - Desc       : Controller for Event management pages (Thymeleaf views)
 * </pre>
 */

import com.bgc.event.dto.EventDto;
import com.bgc.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPageController {

    private final EventService eventService;

    /**
     * Events listing page
     */
    @GetMapping
    public String listEvents(Model model,
                             @PageableDefault(size = 12, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Accessing events listing page");
        
        Page<EventDto> events = eventService.getAllEvents(pageable);
        
        model.addAttribute("currentPage", "events");
        model.addAttribute("pageTitle", "Events");
        model.addAttribute("pageBreadcrumb", "Home / Events");
        model.addAttribute("events", events);
        model.addAttribute("totalEvents", events.getTotalElements());
        
        return "events/list";
    }

    /**
     * Create event page - Redirects to events list with modal open
     * We use a modal for event creation instead of a separate page
     */
    @GetMapping("/create")
    public String createEventForm() {
        log.info("Redirecting to events list with create modal");
        return "redirect:/events?openModal=addEvent";
    }

    /**
     * Edit event page
     */
    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        log.info("Accessing edit event form for ID: {}", id);
        
        try {
            EventDto event = eventService.getEventById(id);
            model.addAttribute("currentPage", "events");
            model.addAttribute("pageTitle", "Edit Event");
            model.addAttribute("pageBreadcrumb", "Home / Events / Edit");
            model.addAttribute("event", event);
            return "events/edit";
        } catch (Exception e) {
            log.error("Event not found: {}", id);
            return "redirect:/events?error=notfound";
        }
    }

    /**
     * Event details page
     */
    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        log.info("Accessing event details page for ID: {}", id);
        
        try {
            EventDto event = eventService.getEventById(id);
            model.addAttribute("currentPage", "events");
            model.addAttribute("pageTitle", event.getTitle());
            model.addAttribute("pageBreadcrumb", "Home / Events / " + event.getTitle());
            model.addAttribute("event", event);
            return "events/view";
        } catch (Exception e) {
            log.error("Event not found: {}", id);
            return "redirect:/events?error=notfound";
        }
    }

    /**
     * Calendar view page
     */
    @GetMapping("/calendar")
    public String calendarView(Model model) {
        log.info("Accessing calendar view");
        
        model.addAttribute("currentPage", "calendar");
        model.addAttribute("pageTitle", "Calendar");
        model.addAttribute("pageBreadcrumb", "Home / Calendar");
        
        return "events/calendar";
    }
}