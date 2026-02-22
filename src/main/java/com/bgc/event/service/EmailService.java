package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : EmailService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for email notifications
 * </pre>
 */

import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;

public interface EmailService {
    
    void sendRegistrationConfirmation(Registration registration, Event event, String qrCodeBase64);
    
    void sendWelcomeWithQrCode(Registration registration, Event event, String qrCodeBase64);
    
    void sendCancellationConfirmation(Registration registration, Event event, String reason);
    
    void sendWaitlistConfirmation(Registration registration, Event event, int position);
    
    void sendWaitlistPromotionNotification(Registration registration, Event event);
    
    void sendEventReminder(Registration registration, Event event);
}