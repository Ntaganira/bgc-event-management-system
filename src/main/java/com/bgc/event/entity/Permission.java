package com.bgc.event.entity;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.entity
 * - File       : Permission.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Permission entity for fine-grained access control
 * </pre>
 */

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {
    
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(name = "description", length = 200)
    private String description;
    
    @Column(name = "resource", length = 50)
    private String resource; // e.g., "EVENT", "USER", "REGISTRATION"
    
    @Column(name = "action", length = 20)
    private String action; // e.g., "CREATE", "READ", "UPDATE", "DELETE"
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}