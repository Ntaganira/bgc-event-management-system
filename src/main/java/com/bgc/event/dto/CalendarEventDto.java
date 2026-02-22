package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : CalendarEventDto.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : DTO for FullCalendar integration
 * </pre>
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDto {
    
    private String id;           // Event ID
    private String title;        // Event title
    private LocalDateTime start;  // Start date/time
    private LocalDateTime end;    // End date/time
    private Boolean allDay;       // All day event?
    
    @JsonProperty("backgroundColor")
    private String color;         // Background color based on status
    private String textColor;      // Text color (white/dark based on background)
    private Boolean editable;      // Can organizer edit?
    private Boolean startEditable; // Can drag to change start?
    private Boolean durationEditable; // Can resize?
    
    private String description;    // Event description
    private String venue;          // Location
    private String status;         // Event status
    private Integer capacity;      // Total capacity
    private Integer registrations; // Current registrations
    private Integer availableSpots; // Available spots
    private String organizer;      // Organizer name
    private String url;            // Link to event details
    
    // Extended properties for custom rendering
    private Map<String, Object> extendedProps;
    
    // For FullCalendar compatibility
    public String getStart() {
        return start != null ? start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
    
    public String getEnd() {
        return end != null ? end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
    
    // Determine text color based on background
    public String getTextColor() {
        if (color == null) return "#000000";
        // Simple logic: dark background -> white text, light background -> black text
        String hex = color.replace("#", "");
        if (hex.length() == 3) {
            hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                  hex.charAt(1) + hex.charAt(1) +
                  hex.charAt(2) + hex.charAt(2);
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
            return luminance > 0.5 ? "#000000" : "#FFFFFF";
        } catch (Exception e) {
            return "#000000";
        }
    }
}