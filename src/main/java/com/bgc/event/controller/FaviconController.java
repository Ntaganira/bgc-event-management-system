package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : FaviconController.java
 * - Date       : 2026. 02. 24.
 * - User       : NTAGANIRA H.
 * - Desc       : Controller to handle favicon requests
 * </pre>
 */

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class FaviconController {

    @GetMapping("favicon.ico")
    @ResponseBody
    public ResponseEntity<Resource> favicon() {
        try {
            Resource resource = new ClassPathResource("static/favicon.ico");
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/x-icon"))
                    .body(resource);
            }
        } catch (Exception e) {
            // Log the error but don't throw it
            System.err.println("Favicon not found: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Return 204 instead of 500
    }
    
    @GetMapping(value = "favicon.svg", produces = "image/svg+xml")
    @ResponseBody
    public String faviconSvg() {
        return "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'>" +
               "<rect width='100' height='100' rx='20' fill='#1a237e'/>" +
               "<text x='50' y='70' font-size='70' text-anchor='middle' fill='white' font-weight='bold'>B</text>" +
               "</svg>";
    }
}