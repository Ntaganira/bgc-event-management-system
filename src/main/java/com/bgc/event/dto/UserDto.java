package com.bgc.event.dto;

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

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String branch;
    private String title;
    private boolean enabled;
    // Profile fields (all optional)
    private String bio;
    private String profilePicture;
    private String linkedinUrl;
    private String websiteUrl;
}
