package com.bgc.event.controller;

/**
 * <pre>
 * - Project : BGC EVENT
 * - File    : EventParticipantController.java
 * - Date    : 2026-02-27
 * - Author  : NTAGANIRA Heritier
 * - Desc    : Manages event participants (Preacher, MC, Speaker, etc.)
 *             Add / remove participants from an event's detail page.
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.EventParticipant;
import com.bgc.event.entity.User;
import com.bgc.event.repository.EventParticipantRepository;
import com.bgc.event.service.EventService;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events/{eventId}/participants")
@RequiredArgsConstructor
public class EventParticipantController {

    private final EventParticipantRepository participantRepository;
    private final EventService               eventService;
    private final UserService                userService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('EDIT_EVENT')")
    public String addParticipant(@PathVariable Long eventId,
                                  @RequestParam Long   userId,
                                  @RequestParam String role,
                                  @RequestParam(required = false) String note,
                                  RedirectAttributes ra) {
        Event event = eventService.findById(eventId).orElseThrow();
        User  user  = userService.findById(userId).orElseThrow();

        if (!participantRepository.existsByEventAndUserAndRole(event, user, role)) {
            participantRepository.save(EventParticipant.builder()
                .event(event)
                .user(user)
                .role(role)
                .note(note != null && !note.isBlank() ? note : null)
                .build());
            ra.addFlashAttribute("successMsg", user.getFullName() + " added as " + role);
        } else {
            ra.addFlashAttribute("errorMsg", user.getFullName() + " is already assigned as " + role);
        }
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{participantId}/remove")
    @PreAuthorize("hasAuthority('EDIT_EVENT')")
    public String removeParticipant(@PathVariable Long eventId,
                                     @PathVariable Long participantId,
                                     RedirectAttributes ra) {
        participantRepository.deleteById(participantId);
        ra.addFlashAttribute("successMsg", "Participant removed.");
        return "redirect:/events/" + eventId;
    }
}
