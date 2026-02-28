package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : PasswordResetServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Secure forgot/reset password flow
 *   - Token expires in 30 minutes
 *   - Previous tokens invalidated on new request
 *   - Anti-enumeration: same response whether email exists or not
 *   - Token is single-use
 * </pre>
 */

import com.bgc.event.entity.PasswordResetToken;
import com.bgc.event.repository.PasswordResetTokenRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender               mailSender;
    private final PasswordEncoder              passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:bgcevent@gmail.com}")
    private String fromEmail;

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    @Override
    public void requestReset(String email) {
        // Anti-enumeration: always appear to succeed regardless of whether email exists
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            // Invalidate any outstanding tokens for this user
            tokenRepository.invalidateAllForUser(user.getId());

            // Generate secure random token
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build();
            tokenRepository.save(resetToken);

            // Send email
            try {
                sendResetEmail(user.getEmail(), user.getFirstName(), token);
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
                // Don't re-throw — keep anti-enumeration intact
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

        // Update password
        var user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used (single-use)
        prt.setUsed(true);
        tokenRepository.save(prt);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void sendResetEmail(String toEmail, String firstName, String token) {
        String resetLink = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("BGC Event — Password Reset Request");
        message.setText(
            "Hello " + firstName + ",\n\n" +
            "We received a request to reset your BGC Event account password.\n\n" +
            "Click the link below to set a new password (valid for " + TOKEN_EXPIRY_MINUTES + " minutes):\n\n" +
            resetLink + "\n\n" +
            "If you did not request a password reset, please ignore this email.\n" +
            "Your password will remain unchanged.\n\n" +
            "— BGC Event Management System"
        );

        mailSender.send(message);
        log.info("Password reset email sent to: {}", toEmail);
    }
}
