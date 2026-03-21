package com.bgc.event.service.impl;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.bgc.event.entity.User;
import com.bgc.event.service.SendEmail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailImpl implements SendEmail {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;
    private final MessageSource messageSource; // ← for i18n strings

    @Value("${spring.mail.username:ntaganira71@gmail.com}")
    private String fromEmail;

    @Override
    public void sendHtmlEmail(User user) throws Exception {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null)
            locale = Locale.ENGLISH;

        // ── i18n strings resolved by MessageSource (not TemplateEngine) ──
        String subject = messageSource.getMessage("auth.register.success.title", null, locale);

        // ── Thymeleaf context — variables injected into the template ──────
        Context ctx = new Context(locale);
        ctx.setVariable("firstName", user.getFirstName());
        ctx.setVariable("subject", subject);

        // ── Render password-reset.html → HTML String ──────────────────────
        String htmlBody = emailTemplateEngine.process("send-email", ctx);

        // ── Plain-text fallback ───────────────────────────────────────────
        String plainBody = buildPlainText(user.getFirstName(), locale);

        // ── Send ──────────────────────────────────────────────────────────
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(fromEmail, "BGC Event Management");
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(plainBody, false); // plain-text part
        helper.setText(htmlBody, true); // HTML part (overrides plain in modern clients)

        mailSender.send(mime);
        log.info("Registration email sent to {} [locale={}]", user.getEmail(), locale);
    }

    /** Plain-text fallback — all strings from MessageSource */
    private String buildPlainText(String firstName, Locale locale) {
        String greeting = messageSource.getMessage("auth.register.success.title", null, locale);
        String intro = messageSource.getMessage("auth.register.success.desc", null, locale);
        return greeting + " " + firstName + ",\n\n"
                + intro + "\n\n"
                + "— BGC Event Management System";
    }
}
