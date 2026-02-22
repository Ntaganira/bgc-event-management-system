package com.bgc.event.controller;

import org.springframework.boot.webmvc.error.ErrorController;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : CustomErrorController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Custom error controller for handling error pages
 * </pre>
 */

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("status", statusCode);
            model.addAttribute("timestamp", LocalDateTime.now());
            model.addAttribute("message", message != null ? message.toString() : null);
            
            if (exception != null) {
                model.addAttribute("trace", exception.toString());
            }
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return "error/400";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }
        
        return "error/default";
    }
}