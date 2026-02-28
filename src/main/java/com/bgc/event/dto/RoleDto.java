package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RoleDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : DTO for Role creation and editing
 * </pre>
 */

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoleDto {
    private Long id;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    private Set<Long> permissionIds = new HashSet<>();
}
