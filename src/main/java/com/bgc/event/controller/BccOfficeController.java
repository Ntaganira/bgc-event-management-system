package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : BccOfficeController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : BCC Office CRUD — top-level section, MANAGE_OFFICES permission
 * </pre>
 */

import com.bgc.event.dto.BccOfficeDto;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.BccOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/offices")
@RequiredArgsConstructor
public class BccOfficeController {

    private final BccOfficeService officeService;
    private final UserRepository   userRepository;
    private final MessageSource    messageSource;

    // ── List ─────────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String list(Model model) {
        model.addAttribute("offices",   officeService.findAll());
        model.addAttribute("countries", officeService.findAllCountries());
        return "offices/list";
    }

    // ── Create ───────────────────────────────────────────────────────────────
    @GetMapping("/new")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String newForm(Model model) {
        model.addAttribute("officeDto", new BccOfficeDto());
        return "offices/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String create(@Valid @ModelAttribute BccOfficeDto officeDto,
                         BindingResult result,
                         RedirectAttributes ra,
                         Locale locale) {
        if (result.hasErrors()) return "offices/form";
        try {
            officeService.create(officeDto);
            ra.addFlashAttribute("successMsg",
                messageSource.getMessage("office.created", null, locale));
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/offices";
    }

    // ── View / Staff ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String view(@PathVariable Long id, Model model) {
        var office = officeService.findById(id).orElseThrow();
        model.addAttribute("office",     office);
        model.addAttribute("staff",      officeService.findStaffByOffice(id));
        model.addAttribute("staffCount", officeService.countStaff(id));
        model.addAttribute("allUsers",   userRepository.findAll().stream()
            .filter(u -> u.getOffice() == null || !u.getOffice().getId().equals(id))
            .toList());
        return "offices/view";
    }

    // ── Edit ─────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String editForm(@PathVariable Long id, Model model) {
        var office = officeService.findById(id).orElseThrow();
        var dto = new BccOfficeDto();
        dto.setId(office.getId());
        dto.setCode(office.getCode());
        dto.setName(office.getName());
        dto.setCountry(office.getCountry());
        dto.setCity(office.getCity());
        dto.setAddress(office.getAddress());
        dto.setPhone(office.getPhone());
        dto.setEmail(office.getEmail());
        dto.setHeadOfOffice(office.getHeadOfOffice());
        dto.setActive(office.isActive());
        model.addAttribute("officeDto", dto);
        return "offices/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute BccOfficeDto officeDto,
                         BindingResult result,
                         RedirectAttributes ra,
                         Locale locale) {
        if (result.hasErrors()) return "offices/form";
        try {
            officeService.update(id, officeDto);
            ra.addFlashAttribute("successMsg",
                messageSource.getMessage("office.updated", null, locale));
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/offices/" + id;
    }

    // ── Toggle active ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String toggle(@PathVariable Long id, RedirectAttributes ra, Locale locale) {
        officeService.toggleActive(id);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("office.updated", null, locale));
        return "redirect:/offices";
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String delete(@PathVariable Long id, RedirectAttributes ra, Locale locale) {
        try {
            officeService.delete(id);
            ra.addFlashAttribute("successMsg",
                messageSource.getMessage("office.deleted", null, locale));
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/offices";
    }

    // ── Assign / Remove staff ─────────────────────────────────────────────────
    @PostMapping("/{id}/staff/add")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String addStaff(@PathVariable Long id,
                           @RequestParam Long userId,
                           RedirectAttributes ra, Locale locale) {
        officeService.assignUserToOffice(userId, id);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("office.staff.added", null, locale));
        return "redirect:/offices/" + id;
    }

    @PostMapping("/{id}/staff/remove")
    @PreAuthorize("hasAuthority('MANAGE_OFFICES')")
    public String removeStaff(@PathVariable Long id,
                              @RequestParam Long userId,
                              RedirectAttributes ra, Locale locale) {
        officeService.removeUserFromOffice(userId);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("office.staff.removed", null, locale));
        return "redirect:/offices/" + id;
    }
}
