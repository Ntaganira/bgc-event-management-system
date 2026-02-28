package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : PasswordResetController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Forgot password + reset password endpoints (public)
 * </pre>
 */

import com.bgc.event.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final MessageSource        messageSource;

    // ── Step 1: Forgot Password form ────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email,
                                       RedirectAttributes ra,
                                       Locale locale) {
        // Always show the same message (anti-enumeration)
        passwordResetService.requestReset(email);
        ra.addFlashAttribute("successMsg",
            messageSource.getMessage("auth.forgot.sent", null, locale));
        return "redirect:/forgot-password?sent=true";
    }

    // ── Step 2: Reset Password form ─────────────────────────────────────────

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        try {
            passwordResetService.validateToken(token);
            model.addAttribute("token", token);
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("tokenInvalid", true);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      Model model,
                                      RedirectAttributes ra,
                                      Locale locale) {
        // Client-side match already checked via JS; double-check server-side
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg",
                messageSource.getMessage("auth.reset.mismatch", null, locale));
            return "auth/reset-password";
        }
        if (password.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg",
                messageSource.getMessage("validation.password.min", null, locale));
            return "auth/reset-password";
        }
        try {
            passwordResetService.resetPassword(token, password);
            ra.addFlashAttribute("successMsg",
                messageSource.getMessage("auth.reset.success", null, locale));
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/reset-password";
        }
    }
}
