package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : CheckInRequest.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Request DTO for attendee check-in
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    
    @NotNull(message = "Event ID is required")
    private Long eventId;
    
    private Long registrationId; // For manual check-in
    
    private String qrCode; // For QR code check-in
    
    @NotBlank(message = "Check-in method is required")
    private String checkInMethod; // "MANUAL" or "QR_SCAN"
    
    private Double latitude; // For location verification
    private Double longitude;
    
    private String deviceInfo;
    
    private String notes;
}