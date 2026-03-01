package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : PasswordResetServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Forgot/reset password — sends a styled HTML email
 *                using MimeMessage + MimeMessageHelper (not SimpleMailMessage).
 *
 *   Security:
 *   - Anti-enumeration: same response whether email exists or not
 *   - Single-use token, expires in 30 minutes
 *   - Previous tokens invalidated before issuing new one
 * </pre>
 */

import com.bgc.event.entity.PasswordResetToken;
import com.bgc.event.repository.PasswordResetTokenRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.PasswordResetService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final JavaMailSender mailSender;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  @Value("${spring.mail.username:ntaganira71@gmail.com}")
  private String fromEmail;

  private static final int TOKEN_EXPIRY_MINUTES = 30;
  private static final String BRAND_COLOR = "#2563EB";
  private static final String BRAND_DARK = "#1E3A5F";

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
        sendHtmlResetEmail(user.getEmail(), user.getFirstName(), token);
      } catch (Exception e) {
        log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
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

    var user = prt.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    prt.setUsed(true);
    tokenRepository.save(prt);

    log.info("Password reset successfully for user: {}", user.getEmail());
  }

  // ── HTML Email ────────────────────────────────────────────────────────

  private void sendHtmlResetEmail(String toEmail, String firstName, String token)
      throws Exception {

    String resetLink = baseUrl + "/reset-password?token=" + token;

    MimeMessage mime = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

    helper.setFrom(fromEmail, "BGC Event Management");
    helper.setTo(toEmail);
    helper.setSubject("Reset your BGC Event password");

    helper.setText(buildPlainText(firstName, resetLink), false); // fallback
    helper.setText(buildHtml(firstName, resetLink), true); // HTML (overrides plain)

    mailSender.send(mime);
    log.info("HTML password reset email sent to: {}", toEmail);
  }

  // ── HTML body ─────────────────────────────────────────────────────────

  private String buildHtml(String firstName, String resetLink) {
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Reset your password</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">

          <!-- Wrapper -->
          <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                 style="background-color:#f1f5f9;padding:40px 16px;">
            <tr>
              <td align="center">

                <!-- Card -->
                <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                       style="max-width:560px;background:#ffffff;border-radius:12px;
                              box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden;">

                  <!-- Header band -->
                  <tr>
                    <td style="background:%s;padding:32px 40px;text-align:center;">
                      <p style="margin:0;font-size:28px;font-weight:800;
                                color:#ffffff;letter-spacing:2px;font-family:Arial,sans-serif;">
                        BGC
                      </p>
                      <p style="margin:6px 0 0;font-size:13px;color:rgba(255,255,255,0.75);
                                letter-spacing:0.5px;text-transform:uppercase;">
                        Event Management System
                      </p>
                    </td>
                  </tr>

                  <!-- Icon row -->
                  <tr>
                    <td style="text-align:center;padding:32px 40px 0;">
                      <div style="display:inline-block;background:#eff6ff;border-radius:50%%;
                                  width:64px;height:64px;line-height:64px;font-size:28px;">
                        🔑
                      </div>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:24px 40px 8px;">
                      <h1 style="margin:0 0 8px;font-size:22px;font-weight:700;color:%s;">
                        Password Reset Request
                      </h1>
                      <p style="margin:0 0 16px;font-size:15px;color:#374151;line-height:1.6;">
                        Hello <strong>%s</strong>,
                      </p>
                      <p style="margin:0 0 24px;font-size:15px;color:#374151;line-height:1.6;">
                        We received a request to reset the password for your BGC Event account.
                        Click the button below to choose a new password.
                      </p>
                    </td>
                  </tr>

                  <!-- CTA Button -->
                  <tr>
                    <td style="padding:0 40px 24px;text-align:center;">
                      <a href="%s"
                         style="display:inline-block;background:%s;color:#ffffff;
                                text-decoration:none;font-size:15px;font-weight:600;
                                padding:14px 36px;border-radius:8px;
                                letter-spacing:0.3px;">
                        Reset My Password
                      </a>
                    </td>
                  </tr>

                  <!-- Expiry notice -->
                  <tr>
                    <td style="padding:0 40px 24px;">
                      <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                          <td style="background:#fef9c3;border-left:4px solid #f59e0b;
                                      border-radius:6px;padding:12px 16px;">
                            <p style="margin:0;font-size:13px;color:#92400e;line-height:1.5;">
                              ⏱ <strong>This link expires in %d minutes.</strong>
                              If you did not request a password reset, you can safely ignore this email —
                              your password will remain unchanged.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- Raw link fallback -->
                  <tr>
                    <td style="padding:0 40px 28px;">
                      <p style="margin:0 0 6px;font-size:12px;color:#9ca3af;">
                        If the button doesn't work, copy and paste this link into your browser:
                      </p>
                      <p style="margin:0;font-size:12px;color:%s;word-break:break-all;">
                        %s
                      </p>
                    </td>
                  </tr>

                  <!-- Divider -->
                  <tr>
                    <td style="padding:0 40px;">
                      <hr style="border:none;border-top:1px solid #e5e7eb;margin:0;">
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="padding:20px 40px 32px;text-align:center;">
                      <p style="margin:0;font-size:12px;color:#9ca3af;line-height:1.6;">
                        This email was sent by <strong>BGC Event Management System</strong>.<br>
                        If you have questions, contact your system administrator.
                      </p>
                    </td>
                  </tr>

                </table>
                <!-- /Card -->

              </td>
            </tr>
          </table>

        </body>
        </html>
        """.formatted(
        BRAND_COLOR, // header background
        BRAND_DARK, // h1 color
        firstName, // greeting
        resetLink, // button href
        BRAND_COLOR, // button background
        TOKEN_EXPIRY_MINUTES,
        BRAND_COLOR, // raw link color
        resetLink // raw link text
    );
  }

  // ── Plain-text fallback (for clients that block HTML) ─────────────────

  private String buildPlainText(String firstName, String resetLink) {
    return """
        Hello %s,

        We received a request to reset your BGC Event account password.

        Click the link below to set a new password (valid for %d minutes):

        %s

        If you did not request a password reset, please ignore this email.
        Your password will remain unchanged.

        — BGC Event Management System
        """.formatted(firstName, TOKEN_EXPIRY_MINUTES, resetLink);
  }
}
