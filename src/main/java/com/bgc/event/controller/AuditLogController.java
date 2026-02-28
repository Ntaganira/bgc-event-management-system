package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AuditLogController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : View and search audit logs
 * </pre>
 */

import com.bgc.event.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOGS')")
    public String auditLogs(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String search,
                            Model model) {
        var pageable = PageRequest.of(page, 20);
        var logs = (search != null && !search.isBlank())
            ? auditLogService.search(search, pageable)
            : auditLogService.findAll(pageable);
        model.addAttribute("logs", logs);
        model.addAttribute("search", search);
        return "audit/list";
    }
}
