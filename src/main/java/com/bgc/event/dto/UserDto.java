package com.bgc.event.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.bgc.event.entity.Role;
import com.bgc.event.entity.User;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : UserDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Data Transfer Object for User entity
 * </pre>
 */

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String branch;
    private String title;
    private boolean enabled;
    private List<String> roles;

    public UserDto(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles().stream()
                         .map(Role::getName)
                         .collect(Collectors.toList());
    }
}
