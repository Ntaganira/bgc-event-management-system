package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RegistrationDetailsDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Detailed registration DTO for admin views
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
public class RegistrationDetailsDto {
    private Long id;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String organization;
    private String jobTitle;
    private String qrCode;
    private String status;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String specialRequirements;
    private String dietaryRestrictions;
    private Long eventId;
    private String eventTitle;
    private String fullName;
    private boolean canCheckIn;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean getCanCheckIn() {
        return !checkedIn && status.equals("CONFIRMED");
    }
}