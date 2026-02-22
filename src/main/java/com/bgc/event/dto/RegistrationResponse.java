package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RegistrationResponse.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Response DTO for registration confirmation
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private Long registrationId;
    private String registrationNumber;
    private String qrCode;
    private String qrCodeBase64; // For displaying QR code in email/web
    private String firstName;
    private String lastName;
    private String email;
    private String eventTitle;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private String eventVenue;
    private String status;
    private LocalDateTime registeredAt;
    private String confirmationToken;
    private String confirmationLink;
    private String calendarLink; // Link to add to calendar
    private boolean isWaitlisted;
    private int waitlistPosition;
    private String message;
}