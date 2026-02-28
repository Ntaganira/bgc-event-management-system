package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : RoleController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Role CRUD + permission assignment controller
 * </pre>
 */

import com.bgc.event.dto.RoleDto;
import com.bgc.event.repository.PermissionRepository;
import com.bgc.event.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Permission;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRolesWithPermissions());
        return "roles/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String newRoleForm(Model model) {
        model.addAttribute("roleDto", new RoleDto());
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "roles/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String createRole(@Valid @ModelAttribute RoleDto roleDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("allPermissions", permissionRepository.findAll());
            return "roles/form";
        }
        try {
            roleService.create(roleDto);
            ra.addFlashAttribute("successMsg", "Role created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/roles";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String editRoleForm(@PathVariable Long id, Model model) {
        var role = roleService.findById(id).orElseThrow();
        var dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        permissionRepository.findPermissionsByRoleId(role.getId()).forEach(p -> dto.getPermissionIds().add(p.getId()));
        model.addAttribute("roleDto", dto);
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "roles/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String updateRole(@PathVariable Long id,
                             @Valid @ModelAttribute RoleDto roleDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("allPermissions", permissionRepository.findAll());
            return "roles/form";
        }
        try {
            roleService.update(id, roleDto);
            ra.addFlashAttribute("successMsg", "Role updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/roles";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String deleteRole(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roleService.delete(id);
            ra.addFlashAttribute("successMsg", "Role deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/roles";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String viewRole(@PathVariable Long id, Model model) {
        model.addAttribute("role", roleService.findById(id).orElseThrow());
        model.addAttribute("allPermissions", permissionRepository.findAll());
        return "roles/view";
    }

    @PostMapping("/{id}/permission/add")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String addPermission(@PathVariable Long id, @RequestParam Long permissionId, RedirectAttributes ra) {
        roleService.assignPermission(id, permissionId);
        ra.addFlashAttribute("successMsg", "Permission assigned.");
        return "redirect:/roles/" + id;
    }

    @PostMapping("/{id}/permission/remove")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public String removePermission(@PathVariable Long id, @RequestParam Long permissionId, RedirectAttributes ra) {
        roleService.removePermission(id, permissionId);
        ra.addFlashAttribute("successMsg", "Permission removed.");
        return "redirect:/roles/" + id;
    }
}
