package com.bgc.event.security;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.security
 * - File       : CurrentUser.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Annotation to inject current authenticated user
 * </pre>
 */

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}