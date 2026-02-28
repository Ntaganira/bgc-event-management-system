package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : User.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : User entity with roles and audit info
 * </pre>
 */

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 100)
    private String branch;

    @Column(length = 100)
    private String title;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "email_confirmed")
    private boolean emailConfirmed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * LAZY: roles are only fetched when explicitly accessed.
     * Spring Security's CustomUserDetailsService calls getRoles() inside
     * a @Transactional method, so the session is still open at that point.
     * Use JOIN FETCH in queries when you need roles loaded upfront.
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "attendees", fetch = FetchType.LAZY)
    private Set<Event> events = new HashSet<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
