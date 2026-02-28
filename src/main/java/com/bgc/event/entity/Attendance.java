package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : Attendance.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Attendance entity - tracks QR/CODE check-ins
 * </pre>
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceMethod method;

    @Column(name = "attendance_time")
    private LocalDateTime attendanceTime;

    @PrePersist
    protected void onCreate() {
        this.attendanceTime = LocalDateTime.now();
    }

    public enum AttendanceMethod {
        QR, CODE
    }
}
