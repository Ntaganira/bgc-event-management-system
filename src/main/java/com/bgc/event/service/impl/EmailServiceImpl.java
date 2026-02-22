package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : EmailServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Email Service with HTML templates
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import com.bgc.event.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
    
    @Override
    @Async
    public void sendRegistrationConfirmation(Registration registration, Event event, String qrCodeBase64) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            context.setVariable("eventVenue", event.getVenue());
            context.setVariable("confirmationLink", 
                    baseUrl + "/confirm?token=" + registration.getRegistrationToken());
            context.setVariable("qrCode", "data:image/png;base64," + qrCodeBase64);
            
            String htmlContent = templateEngine.process("email/registration-confirmation", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "Confirm Your Registration for " + event.getTitle(),
                    htmlContent
            );
            
            log.info("Registration confirmation email sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration confirmation email", e);
        }
    }
    
    @Override
    @Async
    public void sendWelcomeWithQrCode(Registration registration, Event event, String qrCodeBase64) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            context.setVariable("eventVenue", event.getVenue());
            context.setVariable("eventAddress", event.getAddress() + ", " + event.getCity());
            context.setVariable("calendarLink", generateCalendarLink(event));
            context.setVariable("qrCode", "data:image/png;base64," + qrCodeBase64);
            
            String htmlContent = templateEngine.process("email/welcome-with-qr", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "Your Ticket for " + event.getTitle() + " is Ready!",
                    htmlContent
            );
            
            log.info("Welcome email with QR sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }
    }
    
    @Override
    @Async
    public void sendCancellationConfirmation(Registration registration, Event event, String reason) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            context.setVariable("reason", reason != null ? reason : "No reason provided");
            
            String htmlContent = templateEngine.process("email/cancellation-confirmation", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "Registration Cancelled: " + event.getTitle(),
                    htmlContent
            );
            
            log.info("Cancellation confirmation email sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send cancellation email", e);
        }
    }
    
    @Override
    @Async
    public void sendWaitlistConfirmation(Registration registration, Event event, int position) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("position", position);
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            
            String htmlContent = templateEngine.process("email/waitlist-confirmation", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "You're on the Waitlist for " + event.getTitle(),
                    htmlContent
            );
            
            log.info("Waitlist confirmation email sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send waitlist confirmation email", e);
        }
    }
    
    @Override
    @Async
    public void sendWaitlistPromotionNotification(Registration registration, Event event) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            context.setVariable("eventVenue", event.getVenue());
            context.setVariable("confirmationLink", 
                    baseUrl + "/confirm?token=" + registration.getRegistrationToken());
            
            String htmlContent = templateEngine.process("email/waitlist-promotion", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "A Spot Opened Up for " + event.getTitle() + "!",
                    htmlContent
            );
            
            log.info("Waitlist promotion email sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send waitlist promotion email", e);
        }
    }
    
    @Override
    @Async
    public void sendEventReminder(Registration registration, Event event) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", event.getTitle());
            context.setVariable("eventDate", event.getStartDate().format(DATE_FORMATTER));
            context.setVariable("eventVenue", event.getVenue());
            context.setVariable("eventAddress", event.getAddress() + ", " + event.getCity());
            context.setVariable("daysUntil", 
                    java.time.Duration.between(LocalDateTime.now(), event.getStartDate()).toDays());
            context.setVariable("qrCode", "data:image/png;base64," + 
                    Base64.getEncoder().encodeToString(registration.getQrCode().getBytes()));
            
            String htmlContent = templateEngine.process("email/event-reminder", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "Reminder: " + event.getTitle() + " is Coming Up!",
                    htmlContent
            );
            
            log.info("Event reminder email sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send event reminder email", e);
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String email, String token) {
        try {
            Context context = new Context();
            context.setVariable("resetLink", baseUrl + "/reset-password?token=" + token);
            context.setVariable("expiryHours", 24);
            
            String htmlContent = templateEngine.process("email/password-reset", context);
            
            sendHtmlEmail(
                    email,
                    "Reset Your BGC Events Password",
                    htmlContent
            );
            
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
        }
    }
    
    @Async
    public void sendEventUpdateNotification(Registration registration, Event oldEvent, Event newEvent) {
        try {
            Context context = new Context();
            context.setVariable("name", registration.getFirstName() + " " + registration.getLastName());
            context.setVariable("eventTitle", newEvent.getTitle());
            context.setVariable("oldDate", oldEvent.getStartDate().format(DATE_FORMATTER));
            context.setVariable("newDate", newEvent.getStartDate().format(DATE_FORMATTER));
            context.setVariable("oldVenue", oldEvent.getVenue());
            context.setVariable("newVenue", newEvent.getVenue());
            context.setVariable("eventLink", baseUrl + "/events/" + newEvent.getId());
            
            String htmlContent = templateEngine.process("email/event-updated", context);
            
            sendHtmlEmail(
                    registration.getEmail(),
                    "Event Details Updated: " + newEvent.getTitle(),
                    htmlContent
            );
            
            log.info("Event update notification sent to: {}", registration.getEmail());
        } catch (Exception e) {
            log.error("Failed to send event update notification", e);
        }
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String generateCalendarLink(Event event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String start = formatter.format(event.getStartDate());
        String end = formatter.format(event.getEndDate());
        
        return String.format(
            "https://www.google.com/calendar/render?action=TEMPLATE&text=%s&dates=%s/%s&details=%s&location=%s",
            event.getTitle().replace(" ", "+"),
            start,
            end,
            event.getDescription() != null ? event.getDescription().replace(" ", "+") : "",
            event.getVenue().replace(" ", "+")
        );
    }
}