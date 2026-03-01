package com.bgc.event.audit;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.audit
 * - File       : AuditAspect.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : AOP aspect — intercepts every @Auditable method and writes
 *                an audit log entry asynchronously.
 *
 * Strategy:
 *  - @AfterReturning  → log ONLY if the method completed without exception
 *  - @AfterThrowing   → log FAILED_ prefixed action if the method threw
 *  - IP extracted from the current HttpServletRequest (if available)
 *  - Username extracted from the Spring Security context
 *  - Entity ID resolved via SpEL from the method arguments
 * </pre>
 */

import com.bgc.event.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    // ── Success path ──────────────────────────────────────────────────────
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void afterSuccess(JoinPoint jp, Auditable auditable, Object result) {
        writeLog(jp, auditable, auditable.action(), null);
    }

    // ── Failure path ─────────────────────────────────────────────────────
    @AfterThrowing(
        pointcut = "@annotation(auditable)",
        throwing  = "ex"
    )
    public void afterFailure(JoinPoint jp, Auditable auditable, Throwable ex) {
        writeLog(jp, auditable, "FAILED_" + auditable.action(), ex.getMessage());
    }

    // ── Core writer ───────────────────────────────────────────────────────
    private void writeLog(JoinPoint jp, Auditable auditable, String action, String errorDetail) {
        try {
            String username = resolveUsername();
            String entityId = resolveEntityId(jp, auditable.idExpression());
            String details  = errorDetail;

            auditLogService.log(
                username,
                action,
                auditable.entity().isBlank() ? jp.getSignature().getDeclaringType().getSimpleName() : auditable.entity(),
                entityId,
                details,
                resolveIp()
            );
        } catch (Exception e) {
            // Audit must never break the application
            log.warn("AuditAspect failed to write log for action={}: {}", action, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveEntityId(JoinPoint jp, String expression) {
        if (expression == null || expression.isBlank()) return null;
        try {
            MethodSignature sig    = (MethodSignature) jp.getSignature();
            Parameter[]     params = sig.getMethod().getParameters();
            Object[]        args   = jp.getArgs();

            EvaluationContext ctx = new StandardEvaluationContext();
            for (int i = 0; i < params.length; i++) {
                ctx.setVariable(params[i].getName(), args[i]);
            }
            Object val = spelParser.parseExpression(expression).getValue(ctx);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            log.debug("Could not resolve idExpression '{}': {}", expression, e.getMessage());
            return null;
        }
    }
}
