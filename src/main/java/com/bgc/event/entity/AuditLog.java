package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : AuditLog.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Audit log entity for tracking all system actions
 * </pre>
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_name", length = 100)
    private String entityName;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
