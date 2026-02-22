package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Data Transfer Object for Event entity
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;
    
    @NotBlank(message = "Venue is required")
    @Size(max = 500, message = "Venue cannot exceed 500 characters")
    private String venue;
    
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
    
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @Future(message = "Registration deadline must be in the future")
    private LocalDateTime registrationDeadline;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer capacity;
    
    private Integer currentRegistrations;
    
    @Min(value = 0, message = "Waitlist capacity cannot be negative")
    private Integer waitlistCapacity;
    
    private Integer currentWaitlist;
    
    private String status;
    
    private String featuredImage;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color code format")
    private String colorCode;
    
    private String termsAndConditions;
    
    private boolean allowWaitlist;
    
    private boolean requireApproval;
    
    private Set<String> tags;
    
    private Long organizerId;
    
    private String organizerName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean isPublished;
    
    @Builder.Default
    private boolean isFull = false;

    @Builder.Default
    private boolean isRegistrationOpen = false;

    @Builder.Default
    private int availableSpots = 0;
    
    // Derived fields
    public boolean getIsFull() {
        return capacity != null && currentRegistrations != null && 
               currentRegistrations >= capacity;
    }
    
    public boolean getIsRegistrationOpen() {
        return status.equals("OPEN") && 
               (registrationDeadline == null || registrationDeadline.isAfter(LocalDateTime.now())) &&
               !getIsFull();
    }
    
    public int getAvailableSpots() {
        if (capacity == null || currentRegistrations == null) return 0;
        return Math.max(0, capacity - currentRegistrations);
    }
}