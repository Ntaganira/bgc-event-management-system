package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : PasswordResetServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Forgot/reset password — email rendered via Thymeleaf template.
 *                MessageSource resolves i18n keys (subject, plain-text fallback).
 *                TemplateEngine renders the HTML body from password-reset.html.
 * </pre>
 */

import com.bgc.event.entity.PasswordResetToken;
import com.bgc.event.entity.User;
import com.bgc.event.repository.PasswordResetTokenRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.PasswordResetService;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine emailTemplateEngine;
    private final MessageSource messageSource; // ← for i18n strings

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:bgcevent@gmail.com}")
    private String fromEmail;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder,
            @Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine,
            MessageSource messageSource) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.emailTemplateEngine = emailTemplateEngine;
        this.messageSource = messageSource;
    }

    // ── Public API ────────────────────────────────────────────────────────

    @Override
    public void requestReset(String email) {
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            tokenRepository.invalidateAllForUser(user.getId());

            String token = UUID.randomUUID().toString();
            tokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                    .build());

            try {
                sendHtmlEmail(user, token);
            } catch (Exception e) {
                log.error("Failed to send reset email to {}: {}", email, e.getMessage());
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public String validateToken(String token) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));
        if (!prt.isValid())
            throw new RuntimeException("This reset link has expired or has already been used.");
        return prt.getUser().getEmail();
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset link."));
        if (!prt.isValid())
            throw new RuntimeException("This reset link has expired or has already been used.");

        prt.getUser().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(prt.getUser());
        prt.setUsed(true);
        tokenRepository.save(prt);
        log.info("Password reset successfully for: {}", prt.getUser().getEmail());
    }

    // ── Email ─────────────────────────────────────────────────────────────

    private void sendHtmlEmail(User user, String token) throws Exception {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null)
            locale = Locale.ENGLISH;

        String resetLink = baseUrl + "/reset-password?token=" + token;

        // ── i18n strings resolved by MessageSource (not TemplateEngine) ──
        String subject = messageSource.getMessage("email.reset.subject", null, locale);

        // ── Thymeleaf context — variables injected into the template ──────
        Context ctx = new Context(locale);
        ctx.setVariable("firstName", user.getFirstName());
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiryMins", TOKEN_EXPIRY_MINUTES);
        ctx.setVariable("subject", subject);

        // ── Render password-reset.html → HTML String ──────────────────────
        String htmlBody = emailTemplateEngine.process("password-reset", ctx);

        // ── Plain-text fallback ───────────────────────────────────────────
        String plainBody = buildPlainText(user.getFirstName(), resetLink, locale);

        // ── Send ──────────────────────────────────────────────────────────
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(fromEmail, "BGC Event Management");
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(plainBody, false); // plain-text part
        helper.setText(htmlBody, true); // HTML part (overrides plain in modern clients)

        mailSender.send(mime);
        log.info("Reset email sent to {} [locale={}]", user.getEmail(), locale);
    }

    /** Plain-text fallback — all strings from MessageSource */
    private String buildPlainText(String firstName, String resetLink, Locale locale) {
        String greeting = messageSource.getMessage("email.reset.greeting", null, locale);
        String intro = messageSource.getMessage("email.reset.intro", null, locale);
        String expiry = messageSource.getMessage("email.reset.expiry",
                new Object[] { TOKEN_EXPIRY_MINUTES }, locale);
        return greeting + " " + firstName + ",\n\n"
                + intro + "\n\n"
                + resetLink + "\n\n"
                + expiry + "\n\n"
                + "— BGC Event Management System";
    }
}
