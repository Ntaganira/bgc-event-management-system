package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : EventDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : DTO for Event creation and editing
 * </pre>
 */

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String location;

    @NotNull(message = "Start date/time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @NotNull(message = "End date/time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;
}
