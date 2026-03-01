package com.bgc.event.audit;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.audit
 * - File       : Auditable.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Marks a method for automatic audit logging via AOP.
 *                Place on any controller method that mutates state.
 *
 * Usage:
 *   @Auditable(action = "CREATE_EVENT", entity = "Event")
 *   public String createEvent(...) { ... }
 * </pre>
 */

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Action label written to audit_logs.action — use SCREAMING_SNAKE_CASE */
    String action();

    /** Entity type label written to audit_logs.entity_name */
    String entity() default "";

    /**
     * SpEL expression evaluated against method args to extract the entity ID.
     * Examples:
     *   idExpression = "#id"              — @PathVariable Long id
     *   idExpression = "#dto.id"          — DTO with getId()
     *   idExpression = "#eventId"         — @RequestParam Long eventId
     * Leave blank to omit entity ID from the log entry.
     */
    String idExpression() default "";
}
