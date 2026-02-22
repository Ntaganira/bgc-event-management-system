package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EmailCheckResponse.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Response DTO for email check
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCheckResponse {
    private boolean registered;
    private String message;
    
    public EmailCheckResponse(boolean registered) {
        this.registered = registered;
        this.message = registered ? "Email already registered" : "Email available";
    }
}