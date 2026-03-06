package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : UserController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.audit.Auditable;
import com.bgc.event.repository.RoleRepository;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService       userService;
    private final RoleRepository    roleRepository;
    private final MessageSource     messageSource;
    private static final int PAGE_SIZE = 15;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public String listUsers(@RequestParam(defaultValue = "0")  int    page,
                            @RequestParam(defaultValue = "")   String search,
                            Model model) {
        var pageable  = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        model.addAttribute("usersPage", userService.findPaginated(search, pageable));
        model.addAttribute("search",    search);
        return "users/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user",     userService.findById(id).orElseThrow());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users/view";
    }

    @Auditable(action = "TOGGLE_USER", entity = "User", idExpression = "#id")
    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra, Locale locale) {
        userService.toggleEnabled(id);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("user.updated", null, locale));
        return "redirect:/users";
    }

    @Auditable(action = "ASSIGN_ROLE", entity = "User", idExpression = "#id")
    @PostMapping("/{id}/role/add")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String addRole(@PathVariable Long id, @RequestParam Long roleId,
                          RedirectAttributes ra, Locale locale) {
        userService.addRole(id, roleId);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("user.role.added", null, locale));
        return "redirect:/users/" + id;
    }

    @Auditable(action = "REMOVE_ROLE", entity = "User", idExpression = "#id")
    @PostMapping("/{id}/role/remove")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String removeRole(@PathVariable Long id, @RequestParam Long roleId,
                             RedirectAttributes ra, Locale locale) {
        userService.removeRole(id, roleId);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("user.role.removed", null, locale));
        return "redirect:/users/" + id;
    }

    @Auditable(action = "DELETE_USER", entity = "User", idExpression = "#id")
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra, Locale locale) {
        userService.deleteById(id);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("user.deleted", null, locale));
        return "redirect:/users";
    }
}
