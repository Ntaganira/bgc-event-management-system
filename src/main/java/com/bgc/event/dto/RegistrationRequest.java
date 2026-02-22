package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RegistrationRequest.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Request DTO for public event registration
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-']+$", message = "First name can only contain letters, spaces, hyphens and apostrophes")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s-']+$", message = "Last name can only contain letters, spaces, hyphens and apostrophes")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Size(max = 200, message = "Organization cannot exceed 200 characters")
    private String organization;
    
    @Size(max = 100, message = "Job title cannot exceed 100 characters")
    private String jobTitle;
    
    @Size(max = 1000, message = "Special requirements cannot exceed 1000 characters")
    private String specialRequirements;
    
    @Size(max = 500, message = "Dietary restrictions cannot exceed 500 characters")
    private String dietaryRestrictions;
    
    @AssertTrue(message = "You must agree to the terms and conditions")
    private boolean agreeToTerms;
    
    private Map<String, Object> additionalFields; // For custom form fields
    
    private String captchaToken; // For bot prevention
}