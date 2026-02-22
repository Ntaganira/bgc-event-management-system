package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : Attendance.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Attendance entity for tracking check-ins (FR-16, FR-17, FR-18, FR-19)
 * </pre>
 */

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances", indexes = {
    @Index(name = "idx_attendance_event", columnList = "event_id"),
    @Index(name = "idx_attendance_registration", columnList = "registration_id"),
    @Index(name = "idx_attendance_checked_in_at", columnList = "checked_in_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;
    
    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;
    
    @Column(name = "checked_in_by")
    private Long checkedInBy; // User ID of organizer who checked them in
    
    @Column(name = "check_in_method", length = 20)
    private String checkInMethod; // "MANUAL", "QR_SCAN", "BULK"
    
    @Column(name = "qr_code_used")
    private String qrCodeUsed;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "device_info", length = 255)
    private String deviceInfo;
    
    @Column(name = "latitude")
    private Double latitude; // For location verification
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        setCheckedInAt(LocalDateTime.now());
    }
}