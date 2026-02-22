package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventUpdateRequest.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Request DTO for event update
 * </pre>
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {
    
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;
    
    @Size(max = 500, message = "Venue cannot exceed 500 characters")
    private String venue;
    
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
    
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @Future(message = "Registration deadline must be in the future")
    private LocalDateTime registrationDeadline;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer capacity;
    
    @Min(value = 0, message = "Waitlist capacity cannot be negative")
    private Integer waitlistCapacity;
    
    private String featuredImage;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color code format")
    private String colorCode;
    
    private String termsAndConditions;
    
    // Boolean fields with explicit getter methods
    private Boolean allowWaitlist;
    private Boolean requireApproval;
    
    private Set<String> tags;
    
    // Explicit getter for allowWaitlist (returns false if null)
    public boolean isAllowWaitlist() {
        return allowWaitlist != null ? allowWaitlist : false;
    }
    
    // Explicit getter for requireApproval (returns false if null)
    public boolean isRequireApproval() {
        return requireApproval != null ? requireApproval : false;
    }
    
    // Helper method to check if any field is present
    public boolean hasUpdates() {
        return title != null || description != null || shortDescription != null ||
               venue != null || address != null || city != null ||
               startDate != null || endDate != null || registrationDeadline != null ||
               capacity != null || waitlistCapacity != null ||
               featuredImage != null || colorCode != null ||
               termsAndConditions != null || allowWaitlist != null || 
               requireApproval != null || (tags != null && !tags.isEmpty());
    }
}