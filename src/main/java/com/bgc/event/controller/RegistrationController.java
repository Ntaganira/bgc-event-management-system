package com.bgc.event.controller;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.controller
 * - File       : RegistrationController.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : REST controller for public registration and check-in
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import com.bgc.event.security.CurrentUser;
import com.bgc.event.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    // Public endpoints
    @PostMapping("/public/events/{eventId}/register")
    public ResponseEntity<RegistrationResponse> registerForEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody RegistrationRequest request) 
            throws EventException, RegistrationException {
        
        log.info("Public registration request for event ID: {}", eventId);
        RegistrationResponse response = registrationService.registerForEvent(eventId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/public/confirm")
    public ResponseEntity<RegistrationResponse> confirmRegistration(
            @RequestParam String token) throws RegistrationException {
        
        log.info("Registration confirmation with token: {}", token);
        RegistrationResponse response = registrationService.confirmRegistration(token);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public/registrations/{registrationId}/qr-code")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable Long registrationId,
            @RequestParam String email) throws RegistrationException {
        
        log.info("QR code request for registration ID: {}", registrationId);
        RegistrationDetailsDto registration = registrationService.getRegistrationById(registrationId, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "qr-code.png");
        
        return new ResponseEntity<>(registration.getQrCodeImage(), headers, HttpStatus.OK);
    }
    
    @PostMapping("/public/registrations/{registrationId}/cancel")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable Long registrationId,
            @RequestParam String email,
            @RequestParam(required = false) String reason) throws RegistrationException {
        
        log.info("Cancellation request for registration ID: {}", registrationId);
        registrationService.cancelRegistration(registrationId, email, reason);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/public/events/{eventId}/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmailRegistered(
            @PathVariable Long eventId,
            @RequestParam String email) {
        
        boolean registered = registrationService.isEmailRegistered(eventId, email);
        return ResponseEntity.ok(new EmailCheckResponse(registered));
    }
    
    // Protected endpoints (Admin/Organizer)
    @PostMapping("/events/{eventId}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<CheckInResponse> checkInAttendee(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckInRequest request,
            @CurrentUser Long userId) throws RegistrationException {
        
        log.info("Check-in request for event ID: {}", eventId);
        request.setEventId(eventId);
        CheckInResponse response = registrationService.checkInAttendee(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<RegistrationDetailsDto>> getEventRegistrations(
            @PathVariable Long eventId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("Fetching registrations for event ID: {}", eventId);
        Page<RegistrationDetailsDto> registrations = registrationService.getEventRegistrations(eventId, pageable);
        return ResponseEntity.ok(registrations);
    }
    
    @GetMapping("/events/{eventId}/registrations/checked-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<RegistrationDetailsDto>> getCheckedInAttendees(
            @PathVariable Long eventId) {
        
        log.info("Fetching checked-in attendees for event ID: {}", eventId);
        List<RegistrationDetailsDto> attendees = registrationService.getCheckedInAttendees(eventId);
        return ResponseEntity.ok(attendees);
    }
    
    @GetMapping("/events/{eventId}/registrations/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> exportRegistrationsToExcel(
            @PathVariable Long eventId) throws EventException {
        
        log.info("Exporting registrations to Excel for event ID: {}", eventId);
        byte[] excelData = registrationService.exportRegistrationsToExcel(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        headers.setContentDispositionFormData("attachment", "registrations.xlsx");
        
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
    
    @GetMapping("/events/{eventId}/registrations/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> exportRegistrationsToPdf(
            @PathVariable Long eventId) throws EventException {
        
        log.info("Exporting registrations to PDF for event ID: {}", eventId);
        byte[] pdfData = registrationService.exportRegistrationsToPdf(eventId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "registrations.pdf");
        
        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }
    
    @PostMapping("/events/{eventId}/registrations/send-reminders")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> sendReminderEmails(
            @PathVariable Long eventId,
            @CurrentUser Long userId) throws EventException {
        
        log.info("Sending reminder emails for event ID: {}", eventId);
        registrationService.sendReminderEmails(eventId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/events/{eventId}/registrations/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<RegistrationStatistics> getRegistrationStatistics(
            @PathVariable Long eventId) {
        
        log.info("Fetching registration statistics for event ID: {}", eventId);
        RegistrationStatistics statistics = registrationService.getRegistrationStatistics(eventId);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<RegistrationDetailsDto> getRegistrationByQrCode(
            @PathVariable String qrCode) throws RegistrationException {
        
        log.info("Looking up registration by QR code");
        RegistrationDetailsDto registration = registrationService.getRegistrationByQrCode(qrCode);
        return ResponseEntity.ok(registration);
    }
}