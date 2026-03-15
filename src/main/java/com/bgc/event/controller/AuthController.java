package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : AuthController.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.audit.Auditable;
import com.bgc.event.dto.RegisterDto;
import com.bgc.event.entity.BccOffice;
import com.bgc.event.service.BccOfficeService;
import com.bgc.event.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final BccOfficeService officeService;
    private final MessageSource messageSource;

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("registerDto", new RegisterDto());

        Map<String, List<BccOffice>> officesByCountry = officeService.findActive()
                .stream()
                .collect(Collectors.groupingBy(BccOffice::getCountry));

        model.addAttribute("officesByCountry", officesByCountry);

        return "auth/register";
    }

    @Auditable(action = "REGISTER", entity = "User", idExpression = "#registerDto.email")
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDto registerDto,
            BindingResult result, Model model,
            RedirectAttributes ra, Locale locale) {
        if (result.hasErrors()) {
            model.addAttribute("offices", officeService.findActive());
            return "auth/register";
        }
        try {
            registerDto.setPassword("password123");
            userService.register(registerDto);
            ra.addFlashAttribute("successMsg",
                    messageSource.getMessage("auth.register.success", null, locale));
            return "redirect:/register";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("offices", officeService.findActive());
            return "auth/register";
        }
    }
}
