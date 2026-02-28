package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AuthController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Login / Register with i18n messages
 * </pre>
 */

import com.bgc.event.dto.RegisterDto;
import com.bgc.event.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MessageSource messageSource;

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

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDto registerDto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes ra,
                           Locale locale) {
        if (result.hasErrors()) return "auth/register";
        try {
            userService.register(registerDto);
            ra.addFlashAttribute("successMsg",
                messageSource.getMessage("auth.register.submit", null, locale));
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }
}
