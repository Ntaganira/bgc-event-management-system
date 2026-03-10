package com.bgc.event.entity;

/**
 * <pre>
 * - Project : BGC EVENT
 * - File    : EventParticipant.java
 * - Date    : 2026-02-27
 * - Author  : NTAGANIRA Heritier
 * - Desc    : Join entity linking a User to an Event with a participant role
 *             (PREACHER, MC, SPEAKER, WORSHIP, OTHER).
 *             An event can have multiple preachers, multiple MCs, etc.
 * </pre>
 */

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","user_id","role"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Participant role: PREACHER, MC, SPEAKER, WORSHIP, OTHER */
    @Column(nullable = false, length = 50)
    private String role;

    /** Optional extra note (e.g. "Closing sermon", "Host") */
    @Column(length = 255)
    private String note;

    public enum Role {
        PREACHER, MC, SPEAKER, WORSHIP, OTHER
    }
}
