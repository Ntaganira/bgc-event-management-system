package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : Event.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Event entity for managing events
 * </pre>
 */

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_start_date", columnList = "start_date"),
        @Index(name = "idx_event_organizer", columnList = "organizer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Event extends BaseEntity {

    public enum EventStatus {
        DRAFT,
        OPEN,
        FULL,
        CLOSED,
        CANCELLED,
        COMPLETED,
        PUBLISHED
    }

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "venue", length = 500)
    private String venue;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "capacity")
    private Integer capacity;
    
    @Builder.Default
    @Column(name = "current_registrations")
    private Integer currentRegistrations = 0;
    
    @Builder.Default
    @Column(name = "waitlist_capacity")
    private Integer waitlistCapacity = 0;
    
    @Builder.Default
    @Column(name = "current_waitlist")
    private Integer currentWaitlist = 0;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "featured_image")
    private String featuredImage;
    
    @Builder.Default
    @Column(name = "color_code", length = 7)
    private String colorCode = "#3788d8";

    @Column(name = "terms_and_conditions", length = 5000)
    private String termsAndConditions;
    
    @Builder.Default
    @Column(name = "allow_waitlist")
    private boolean allowWaitlist = false;
    
    @Builder.Default
    @Column(name = "require_approval")
    private boolean requireApproval = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;
    
    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Registration> registrations = new HashSet<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Attendance> attendances = new HashSet<>();
    
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    // ===== DERIVED FIELDS - MARKED AS @Transient =====
    
    @Transient  // This tells Hibernate NOT to map this to a database column
    private int availableSpots;
    
    @Transient  // This tells Hibernate NOT to map this to a database column
    private boolean isFull;
    
    // ===== HELPER METHODS TO CALCULATE DERIVED VALUES =====
    
    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculateDerivedFields() {
        this.availableSpots = calculateAvailableSpots();
        this.isFull = calculateIsFull();
    }
    
    private int calculateAvailableSpots() {
        if (capacity == null || currentRegistrations == null) return 0;
        return Math.max(0, capacity - currentRegistrations);
    }
    
    private boolean calculateIsFull() {
        return capacity != null && currentRegistrations != null && 
               currentRegistrations >= capacity;
    }
    
    // ===== CONVENIENCE METHODS =====
    
    public boolean getIsFull() {
        return isFull;
    }
    
    // Prevent setting derived fields directly
    public void setAvailableSpots(int availableSpots) {
        throw new UnsupportedOperationException("availableSpots is a derived field and cannot be set directly");
    }
    
    public void setFull(boolean full) {
        throw new UnsupportedOperationException("isFull is a derived field and cannot be set directly");
    }
}