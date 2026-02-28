package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : CalendarEventDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : FullCalendar-compatible event JSON DTO
 * </pre>
 */

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarEventDto {
    private String id;
    private String title;
    private String start;   // ISO datetime
    private String end;     // ISO datetime
    private String color;
    private String extendedPropsLocation;
    private String url;
}
