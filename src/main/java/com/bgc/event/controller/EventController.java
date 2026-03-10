package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : EventController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.audit.Auditable;
import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.EventDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService   eventService;
    private final UserRepository userRepository;
    private static final int PAGE_SIZE = 10;

    @GetMapping("/api/events/calendar")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDto>> calendarEvents() {
        return ResponseEntity.ok(eventService.findAllAsCalendarEvents());
    }

    @GetMapping("/events")
    @PreAuthorize("hasAuthority('VIEW_EVENT')")
    public String listEvents(@RequestParam(defaultValue = "0")  int    page,
                             @RequestParam(defaultValue = "")   String search,
                             Model model) {
        var pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("startDateTime").descending());
        Page<Event> eventsPage = eventService.findPaginated(search, pageable);
        model.addAttribute("eventsPage", eventsPage);
        model.addAttribute("search",     search);
        model.addAttribute("currentPage","events");
        return "events/list";
    }

    @GetMapping("/events/calendar")
    @PreAuthorize("hasAuthority('VIEW_EVENT')")
    public String calendarPage() { return "events/calendar"; }

    @GetMapping("/events/{id}")
    @PreAuthorize("hasAuthority('VIEW_EVENT')")
    public String viewEvent(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id).orElseThrow();
        model.addAttribute("event", event);
        model.addAttribute("attendanceCount", event.getAttendanceRecords().size());
        return "events/view";
    }

    @GetMapping("/events/new")
    @PreAuthorize("hasAuthority('CREATE_EVENT')")
    public String newEventForm(Model model) {
        model.addAttribute("eventDto", new EventDto());
        return "events/form";
    }

    @Auditable(action = "CREATE_EVENT", entity = "Event", idExpression = "#dto.title")
    @PostMapping("/events/new")
    @PreAuthorize("hasAuthority('CREATE_EVENT')")
    public String createEvent(@Valid @ModelAttribute EventDto dto, BindingResult result,
                              Authentication auth, RedirectAttributes ra) {
        if (result.hasErrors()) return "events/form";
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        eventService.create(dto, user);
        ra.addFlashAttribute("successMsg", "Event created successfully!");
        return "redirect:/events";
    }

       @GetMapping("/events/{id}/edit")
    @PreAuthorize("hasAuthority('EDIT_EVENT')")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event e = eventService.findById(id).orElseThrow();
        EventDto dto = new EventDto();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setDescription(e.getDescription());
        dto.setLocation(e.getLocation());
        dto.setStartDateTime(e.getStartDateTime());
        dto.setEndDateTime(e.getEndDateTime());
        model.addAttribute("eventDto", dto);
        return "events/form";
    }

    @Auditable(action = "UPDATE_EVENT", entity = "Event", idExpression = "#id")
    @PostMapping("/events/{id}/edit")
    @PreAuthorize("hasAuthority('EDIT_EVENT')")
    public String updateEvent(@PathVariable Long id, @Valid @ModelAttribute EventDto dto,
                              BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "events/form";
        eventService.update(id, dto);
        ra.addFlashAttribute("successMsg", "Event updated successfully!");
        return "redirect:/events";
    }

    @Auditable(action = "DELETE_EVENT", entity = "Event", idExpression = "#id")
    @PostMapping("/events/{id}/delete")
    @PreAuthorize("hasAuthority('DELETE_EVENT')")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        eventService.delete(id);
        ra.addFlashAttribute("successMsg", "Event deleted.");
        return "redirect:/events";
    }
}
