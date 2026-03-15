package com.bgc.event.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : RoleController.java
 * - Date       : 2026-03-06
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SiteController {

    private final MessageSource messageSource;

    @GetMapping({ "/", "/index", "/home" })
    public String indexPage(Model model) {
        return "website/index";
    }

    @GetMapping("/speaker")
    public String speakerPage(Model model) {
        return "website/speaker";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model, Locale locale) {
        if (error != null)
            model.addAttribute("errorMsg",
                    messageSource.getMessage("auth.login.error", null, locale));
        if (logout != null)
            model.addAttribute("logoutMsg",
                    messageSource.getMessage("auth.login.logout", null, locale));
        return "auth/login";
    }

    @GetMapping("/agender")
    public String agenderPage(Model model) {
        return "website/agenda";
    }
}
