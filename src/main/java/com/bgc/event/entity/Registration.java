package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : Registration.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Registration entity for public event registration
 * </pre>
 */

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "email"}),
    @UniqueConstraint(columnNames = {"qr_code"})
}, indexes = {
    @Index(name = "idx_registration_email", columnList = "email"),
    @Index(name = "idx_registration_qr", columnList = "qr_code"),
    @Index(name = "idx_registration_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration extends BaseEntity {
    
    public enum RegistrationStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        ATTENDED,
        NO_SHOW,
        WAITLISTED
    }
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "organization", length = 200)
    private String organization;
    
    @Column(name = "job_title", length = 100)
    private String jobTitle;
    
    @Column(name = "qr_code", unique = true, nullable = false)
    private String qrCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.CONFIRMED;
    @Builder.Default
    @Column(name = "checked_in")
    private boolean checkedIn = false;
    
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "special_requirements", length = 1000)
    private String specialRequirements;
    
    @Column(name = "dietary_restrictions", length = 500)
    private String dietaryRestrictions;
    
    @Column(name = "registration_token", length = 100)
    private String registrationToken;
    
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // Store additional form fields as JSON
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @PrePersist
    public void prePersist() {
        if (qrCode == null) {
            qrCode = UUID.randomUUID().toString();
        }
        if (registrationToken == null) {
            registrationToken = UUID.randomUUID().toString();
            tokenExpiry = LocalDateTime.now().plusDays(7);
        }
    }
}