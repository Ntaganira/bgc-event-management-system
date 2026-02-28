package com.bgc.event.dto;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.dto
 * - File       : BccOfficeDto.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BccOfficeDto {

    private Long id;

    @NotBlank(message = "Office code is required")
    @Size(max = 20, message = "Code must be 20 characters or less")
    private String code;

    @NotBlank(message = "Office name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    private String address;
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    private String headOfOffice;
    private boolean active = true;
}
