package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : RegisterDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : DTO for public user registration form
 * </pre>
 */

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    private String phoneNumber;
    private String branch;
    private String title;
    private LocalDate arrivalDate;
    private LocalDate returnDate;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
