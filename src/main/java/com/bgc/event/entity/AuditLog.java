package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : AuditLog.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Enhanced Audit Log entity for FR-27 compliance
 * </pre>
 */

import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_created", columnList = "created_at"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_category", columnList = "action_category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    
    @Column(name = "action_category", length = 30)
    private String actionCategory;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 50)
    private String username;
    
    @Column(name = "user_email", length = 100)
    private String userEmail;
    
    @Column(name = "user_role", length = 50)
    private String userRole;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "entity_name", length = 200)
    private String entityName;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "request_method", length = 10)
    private String requestMethod;
    
    @Column(name = "request_path", length = 255)
    private String requestPath;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private String oldValues;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private String newValues;
    
    @Column(name = "changes_summary", length = 1000)
    private String changesSummary;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (actionCategory == null) {
            actionCategory = determineCategory(action);
        }
    }
    
    private String determineCategory(String action) {
        if (action == null) return "OTHER";
        
        String upperAction = action.toUpperCase();
        if (upperAction.contains("CREATE") || upperAction.contains("ADD")) return "CREATE";
        if (upperAction.contains("UPDATE") || upperAction.contains("EDIT") || upperAction.contains("MODIFY")) return "UPDATE";
        if (upperAction.contains("DELETE") || upperAction.contains("REMOVE") || upperAction.contains("CANCEL")) return "DELETE";
        if (upperAction.contains("LOGIN")) return "LOGIN";
        if (upperAction.contains("LOGOUT")) return "LOGOUT";
        if (upperAction.contains("EXPORT") || upperAction.contains("DOWNLOAD")) return "EXPORT";
        if (upperAction.contains("VIEW") || upperAction.contains("READ") || upperAction.contains("GET")) return "READ";
        if (upperAction.contains("CHECKIN") || upperAction.contains("CHECK_IN")) return "CHECKIN";
        
        return "OTHER";
    }
}