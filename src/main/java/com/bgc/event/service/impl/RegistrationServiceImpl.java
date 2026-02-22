package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : RegistrationServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Registration Service with QR code generation
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import com.bgc.event.entity.User;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AuditService;
import com.bgc.event.service.EmailService;
import com.bgc.event.service.QrCodeService;
import com.bgc.event.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {
    
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    
    @Override
    @CacheEvict(value = {"event-registrations", "event-statistics"}, key = "#eventId")
    public RegistrationResponse registerForEvent(Long eventId, RegistrationRequest request) 
            throws EventException, RegistrationException {
        
        log.info("Processing registration for event ID: {}, email: {}", eventId, request.getEmail());
        
        // Validate captcha token (implement captcha verification)
        validateCaptcha(request.getCaptchaToken());
        
        // Get event with pessimistic lock to prevent race conditions
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventException("Event not found");
                });
        
        // Check if event is open for registration
        if (!isEventOpenForRegistration(event)) {
            log.warn("Event ID: {} is not open for registration", eventId);
            throw new RegistrationException("Event is not open for registration");
        }
        
        // Check for duplicate registration
        if (registrationRepository.existsByEventIdAndEmail(eventId, request.getEmail())) {
            log.warn("Duplicate registration attempt for event ID: {}, email: {}", eventId, request.getEmail());
            throw new RegistrationException("You have already registered for this event");
        }
        
        // Check capacity and handle waitlist
        boolean isWaitlisted = false;
        int waitlistPosition = 0;
        
        synchronized (this) {
            if (event.getCurrentRegistrations() >= event.getCapacity()) {
                if (event.isAllowWaitlist() && event.getWaitlistCapacity() > 0 && 
                    event.getCurrentWaitlist() < event.getWaitlistCapacity()) {
                    isWaitlisted = true;
                    event.setCurrentWaitlist(event.getCurrentWaitlist() + 1);
                    waitlistPosition = event.getCurrentWaitlist();
                    log.info("Adding to waitlist for event ID: {}, position: {}", eventId, waitlistPosition);
                } else {
                    throw new RegistrationException("Event is full and waitlist is not available");
                }
            } else {
                // Increment registration count
                event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);
                eventRepository.save(event);
            }
        }
        
        // Generate QR code
        String qrCodeContent = generateQrContent(event, request);
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(qrCodeContent);
        
        // Create registration
        Registration registration = Registration.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .organization(request.getOrganization())
                .jobTitle(request.getJobTitle())
                .qrCode(UUID.randomUUID().toString())
                .status(isWaitlisted ? Registration.RegistrationStatus.WAITLISTED : 
                                       Registration.RegistrationStatus.PENDING)
                .specialRequirements(request.getSpecialRequirements())
                .dietaryRestrictions(request.getDietaryRestrictions())
                .registrationToken(UUID.randomUUID().toString())
                .tokenExpiry(LocalDateTime.now().plusDays(7))
                .event(event)
                .build();
        
        // Store additional fields as JSON
        if (request.getAdditionalFields() != null && !request.getAdditionalFields().isEmpty()) {
            try {
                registration.setMetadata(objectMapper.writeValueAsString(request.getAdditionalFields()));
            } catch (Exception e) {
                log.error("Failed to serialize additional fields", e);
            }
        }
        
        Registration savedRegistration = registrationRepository.save(registration);
        
        // Send confirmation email
        sendConfirmationEmail(savedRegistration, qrCodeBase64, isWaitlisted, waitlistPosition);
        
        // Build response
        RegistrationResponse response = buildRegistrationResponse(savedRegistration, event, qrCodeBase64);
        response.setWaitlisted(isWaitlisted);
        response.setWaitlistPosition(waitlistPosition);
        
        if (isWaitlisted) {
            response.setMessage("You have been added to the waitlist. We'll notify you if a spot becomes available.");
        } else {
            response.setMessage("Registration successful! Please check your email for confirmation.");
        }
        
        auditService.logAction("REGISTER", null, "Registration", savedRegistration.getId(), 
                "Registered for event: " + event.getTitle());
        
        log.info("Registration completed for event ID: {}, registration ID: {}", 
                eventId, savedRegistration.getId());
        
        return response;
    }
    
    @Override
    @Transactional
    public RegistrationResponse confirmRegistration(String token) throws RegistrationException {
        log.info("Confirming registration with token: {}", token);
        
        Registration registration = registrationRepository.findByRegistrationToken(token)
                .orElseThrow(() -> {
                    log.error("Invalid confirmation token: {}", token);
                    return new RegistrationException("Invalid or expired confirmation token");
                });
        
        if (registration.getTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error("Expired confirmation token: {}", token);
            throw new RegistrationException("Confirmation token has expired");
        }
        
        if (registration.getStatus() == Registration.RegistrationStatus.CONFIRMED) {
            log.warn("Registration already confirmed: {}", registration.getId());
            return buildRegistrationResponse(registration, registration.getEvent(), null);
        }
        
        registration.setStatus(Registration.RegistrationStatus.CONFIRMED);
        registration.setRegistrationToken(null); // Invalidate token
        registration.setTokenExpiry(null);
        
        Registration confirmedRegistration = registrationRepository.save(registration);
        
        // Send welcome email with QR code
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(registration.getQrCode());
        sendWelcomeEmail(confirmedRegistration, qrCodeBase64);
        
        auditService.logAction("CONFIRM_REGISTRATION", null, "Registration", registration.getId(), 
                "Confirmed registration");
        
        log.info("Registration confirmed: {}", registration.getId());
        
        return buildRegistrationResponse(confirmedRegistration, registration.getEvent(), qrCodeBase64);
    }
    
    @Override
    @CacheEvict(value = {"event-registrations", "event-statistics"}, key = "#eventId")
    public void cancelRegistration(Long registrationId, String email, String reason) 
            throws RegistrationException {
        
        log.info("Cancelling registration ID: {}, email: {}", registrationId, email);
        
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> {
                    log.error("Registration not found with ID: {}", registrationId);
                    return new RegistrationException("Registration not found");
                });
        
        // Verify email matches
        if (!registration.getEmail().equalsIgnoreCase(email)) {
            log.warn("Email mismatch for cancellation: provided {}, expected {}", 
                    email, registration.getEmail());
            throw new RegistrationException("Email does not match registration");
        }
        
        Event event = registration.getEvent();
        
        // Update registration status
        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registration.setCancellationReason(reason);
        registrationRepository.save(registration);
        
        // Update event counts
        synchronized (this) {
            if (registration.getStatus() == Registration.RegistrationStatus.WAITLISTED) {
                event.setCurrentWaitlist(event.getCurrentWaitlist() - 1);
            } else {
                event.setCurrentRegistrations(event.getCurrentRegistrations() - 1);
                
                // Check waitlist and promote if available
                promoteFromWaitlist(event);
            }
            eventRepository.save(event);
        }
        
        // Send cancellation email
        sendCancellationEmail(registration, reason);
        
        auditService.logAction("CANCEL_REGISTRATION", null, "Registration", registrationId, 
                "Cancelled registration. Reason: " + reason);
        
        log.info("Registration cancelled: {}", registrationId);
    }
    
    @Override
    @Cacheable(value = "registrations", key = "#qrCode")
    @Transactional(readOnly = true)
    public RegistrationDetailsDto getRegistrationByQrCode(String qrCode) throws RegistrationException {
        log.debug("Fetching registration by QR code: {}", qrCode);
        
        Registration registration = registrationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> {
                    log.error("Registration not found for QR code: {}", qrCode);
                    return new RegistrationException("Invalid QR code");
                });
        
        return mapToDetailsDto(registration);
    }
    
    @Override
    @CacheEvict(value = "registrations", key = "#request.qrCode")
    public CheckInResponse checkInAttendee(CheckInRequest request, Long userId) 
            throws RegistrationException {
        
        log.info("Processing check-in for event ID: {}, method: {}", 
                request.getEventId(), request.getCheckInMethod());
        
        Registration registration;
        
        if ("QR_SCAN".equals(request.getCheckInMethod()) && request.getQrCode() != null) {
            registration = registrationRepository.findByQrCode(request.getQrCode())
                    .orElseThrow(() -> new RegistrationException("Invalid QR code"));
        } else if ("MANUAL".equals(request.getCheckInMethod()) && request.getRegistrationId() != null) {
            registration = registrationRepository.findById(request.getRegistrationId())
                    .orElseThrow(() -> new RegistrationException("Registration not found"));
        } else {
            throw new RegistrationException("Invalid check-in request");
        }
        
        // Verify event matches
        if (!registration.getEvent().getId().equals(request.getEventId())) {
            log.warn("Registration {} does not belong to event {}", 
                    registration.getId(), request.getEventId());
            throw new RegistrationException("Registration does not belong to this event");
        }
        
        // Check if already checked in
        if (registration.isCheckedIn()) {
            log.warn("Attendee already checked in: {}", registration.getId());
            return CheckInResponse.builder()
                    .success(false)
                    .message("Attendee already checked in at: " + registration.getCheckedInAt())
                    .attendeeName(registration.getFirstName() + " " + registration.getLastName())
                    .build();
        }
        
        // Validate check-in time
        Event event = registration.getEvent();
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(event.getStartDate())) {
            log.warn("Attempted check-in before event start: {}", event.getId());
            return CheckInResponse.builder()
                    .success(false)
                    .message("Check-in not available before event start time")
                    .build();
        }
        
        // Perform check-in
        registration.setCheckedIn(true);
        registration.setCheckedInAt(now);
        registration.setStatus(Registration.RegistrationStatus.ATTENDED);
        registrationRepository.save(registration);
        
        // Get user name for audit
        String userName = "Unknown";
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                userName = user.getFirstName() + " " + user.getLastName();
            }
        }
        
        auditService.logAction("CHECK_IN", userId, "Registration", registration.getId(), 
                "Checked in by: " + userName + ", method: " + request.getCheckInMethod());
        
        log.info("Check-in successful for registration: {}", registration.getId());
        
        return CheckInResponse.builder()
                .success(true)
                .message("Check-in successful")
                .attendeeName(registration.getFirstName() + " " + registration.getLastName())
                .email(registration.getEmail())
                .checkInTime(now)
                .totalCheckedIn(registrationRepository.countCheckedInByEvent(event.getId()))
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationDetailsDto> getEventRegistrations(Long eventId, Pageable pageable) {
        log.debug("Fetching registrations for event ID: {}, page: {}", eventId, pageable);
        
        Page<Registration> registrations = registrationRepository.findByEventId(eventId, pageable);
        return registrations.map(this::mapToDetailsDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RegistrationDetailsDto> getCheckedInAttendees(Long eventId) {
        log.debug("Fetching checked-in attendees for event ID: {}", eventId);
        
        List<Registration> registrations = registrationRepository.findCheckedInAttendees(eventId);
        return registrations.stream().map(this::mapToDetailsDto).collect(Collectors.toList());
    }
    
    @Override
    public byte[] exportRegistrationsToExcel(Long eventId) throws EventException {
        // Implementation would use Apache POI or similar
        // This is a placeholder
        log.info("Exporting registrations to Excel for event ID: {}", eventId);
        return new byte[0];
    }
    
    @Override
    public byte[] exportRegistrationsToPdf(Long eventId) throws EventException {
        // Implementation would use iText or similar
        // This is a placeholder
        log.info("Exporting registrations to PDF for event ID: {}", eventId);
        return new byte[0];
    }
    
    @Override
    public void sendReminderEmails(Long eventId, Long userId) throws EventException {
        log.info("Sending reminder emails for event ID: {}", eventId);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException("Event not found"));
        
        Page<Registration> registrations = registrationRepository.findByEventId(eventId, Pageable.unpaged());
        
        for (Registration registration : registrations.getContent()) {
            if (registration.getStatus() == Registration.RegistrationStatus.CONFIRMED) {
                emailService.sendEventReminder(registration, event);
            }
        }
        
        auditService.logAction("SEND_REMINDERS", userId, "Event", eventId, 
                "Sent reminder emails to " + registrations.getTotalElements() + " attendees");
    }
    
    @Override
    @Transactional(readOnly = true)
    public RegistrationStatistics getRegistrationStatistics(Long eventId) {
        long total = registrationRepository.countByEventId(eventId);
        long checkedIn = registrationRepository.countCheckedInByEvent(eventId);
        int waitlist = registrationRepository.countWaitlistByEvent(eventId);
        
        return RegistrationStatistics.builder()
                .eventId(eventId)
                .totalRegistrations((int) total)
                .confirmedCount((int) registrationRepository.countByEventAndStatus(eventId, "CONFIRMED"))
                .checkedInCount((int) checkedIn)
                .cancelledCount((int) registrationRepository.countByEventAndStatus(eventId, "CANCELLED"))
                .waitlistCount(waitlist)
                .attendanceRate(total > 0 ? (double) checkedIn / total * 100 : 0)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailRegistered(Long eventId, String email) {
        return registrationRepository.existsByEventIdAndEmail(eventId, email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getWaitlistPosition(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId).orElse(null);
        if (registration == null || registration.getStatus() != Registration.RegistrationStatus.WAITLISTED) {
            return -1;
        }
        
        // This would need a custom query to get position
        // Simplified version
        return 0;
    }
    
    private boolean isEventOpenForRegistration(Event event) {
        LocalDateTime now = LocalDateTime.now();
        
        return event.getStatus() == Event.EventStatus.OPEN &&
               (event.getRegistrationDeadline() == null || event.getRegistrationDeadline().isAfter(now)) &&
               (event.getCapacity() == null || event.getCurrentRegistrations() < event.getCapacity() ||
                (event.isAllowWaitlist() && event.getCurrentWaitlist() < event.getWaitlistCapacity()));
    }
    
    private void promoteFromWaitlist(Event event) {
        if (!event.isAllowWaitlist() || event.getCurrentWaitlist() == 0) {
            return;
        }
        
        // Get next waitlisted registration
        List<Registration> waitlist = registrationRepository.findByEventAndStatus(
                event.getId(), Registration.RegistrationStatus.WAITLISTED);
        
        if (!waitlist.isEmpty()) {
            Registration nextInLine = waitlist.get(0);
            nextInLine.setStatus(Registration.RegistrationStatus.PENDING);
            registrationRepository.save(nextInLine);
            
            event.setCurrentWaitlist(event.getCurrentWaitlist() - 1);
            event.setCurrentRegistrations(event.getCurrentRegistrations() + 1);
            
            // Send notification email
            emailService.sendWaitlistPromotionNotification(nextInLine, event);
            
            log.info("Promoted registration {} from waitlist", nextInLine.getId());
        }
    }
    
    private String generateQrContent(Event event, RegistrationRequest request) {
        Map<String, String> data = new HashMap<>();
        data.put("eventId", event.getId().toString());
        data.put("eventTitle", event.getTitle());
        data.put("email", request.getEmail());
        data.put("timestamp", LocalDateTime.now().toString());
        
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to generate QR content", e);
            return UUID.randomUUID().toString();
        }
    }
    
    private void validateCaptcha(String captchaToken) {
        // Implement captcha validation
        // This is a placeholder
        if (captchaToken == null || captchaToken.isEmpty()) {
            throw new RegistrationException("Captcha validation failed");
        }
    }
    
    private void sendConfirmationEmail(Registration registration, String qrCodeBase64, 
                                       boolean isWaitlisted, int waitlistPosition) {
        if (isWaitlisted) {
            emailService.sendWaitlistConfirmation(registration, registration.getEvent(), waitlistPosition);
        } else {
            emailService.sendRegistrationConfirmation(registration, registration.getEvent(), qrCodeBase64);
        }
    }
    
    private void sendWelcomeEmail(Registration registration, String qrCodeBase64) {
        emailService.sendWelcomeWithQrCode(registration, registration.getEvent(), qrCodeBase64);
    }
    
    private void sendCancellationEmail(Registration registration, String reason) {
        emailService.sendCancellationConfirmation(registration, registration.getEvent(), reason);
    }
    
    private RegistrationResponse buildRegistrationResponse(Registration registration, Event event, 
                                                          String qrCodeBase64) {
        String baseUrl = "https://bgc.event"; // Configure this
        
        return RegistrationResponse.builder()
                .registrationId(registration.getId())
                .registrationNumber("REG-" + registration.getId())
                .qrCode(registration.getQrCode())
                .qrCodeBase64(qrCodeBase64)
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .eventTitle(event.getTitle())
                .eventStartDate(event.getStartDate())
                .eventEndDate(event.getEndDate())
                .eventVenue(event.getVenue())
                .status(registration.getStatus().toString())
                .registeredAt(registration.getCreatedAt())
                .confirmationToken(registration.getRegistrationToken())
                .confirmationLink(baseUrl + "/confirm?token=" + registration.getRegistrationToken())
                .calendarLink(generateCalendarLink(event))
                .build();
    }
    
    private String generateCalendarLink(Event event) {
        // Generate Google Calendar link
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
    
    private RegistrationDetailsDto mapToDetailsDto(Registration registration) {
        Event event = registration.getEvent();
        
        return RegistrationDetailsDto.builder()
                .id(registration.getId())
                .registrationNumber("REG-" + registration.getId())
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .phoneNumber(registration.getPhoneNumber())
                .organization(registration.getOrganization())
                .jobTitle(registration.getJobTitle())
                .qrCode(registration.getQrCode())
                .status(registration.getStatus().toString())
                .checkedIn(registration.isCheckedIn())
                .checkedInAt(registration.getCheckedInAt())
                .registeredAt(registration.getCreatedAt())
                .specialRequirements(registration.getSpecialRequirements())
                .dietaryRestrictions(registration.getDietaryRestrictions())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .build();
    }

    @Override
    public RegistrationDetailsDto getRegistrationById(Long registrationId, String email) throws RegistrationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRegistrationById'");
    }

    @Override
    public long countByEventAndStatus(Long eventId, String status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'countByEventAndStatus'");
    }
}