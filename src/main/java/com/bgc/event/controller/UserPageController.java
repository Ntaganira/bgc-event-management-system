package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : UserPageController.java
 * - Date       : 2026. 02. 24.
 * - User       : NTAGANIRA H.
 * - Desc       : Controller for User management pages (Thymeleaf views)
 * </pre>
 */

import com.bgc.event.dto.UserDto;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserPageController {

    private final UserService userService;

    /**
     * Users listing page
     */
    @GetMapping
    public String listUsers(Model model,
                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Accessing users listing page");
        
        Page<UserDto> users = userService.getAllUsers(pageable);
        
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "Users");
        model.addAttribute("pageBreadcrumb", "Administration / Users");
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", users.getTotalElements());
        
        return "users/list";
    }

    /**
     * User details page
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        log.info("Accessing user details page for ID: {}", id);
        
        try {
            UserDto user = userService.getUserById(id);
            model.addAttribute("currentPage", "users");
            model.addAttribute("pageTitle", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("pageBreadcrumb", "Administration / Users / " + user.getFirstName());
            model.addAttribute("user", user);
            return "users/view";
        } catch (Exception e) {
            log.error("User not found: {}", id);
            return "redirect:/users?error=notfound";
        }
    }

    /**
     * Create user page - Redirects to users list with modal open
     */
    @GetMapping("/create")
    public String createUserForm() {
        log.info("Redirecting to users list with create modal");
        return "redirect:/users?openModal=addUser";
    }

    /**
     * Edit user page - Redirects to users list with edit modal
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id) {
        log.info("Redirecting to users list with edit modal for ID: {}", id);
        return "redirect:/users?openModal=editUser&userId=" + id;
    }
}