package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : RegistrationService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for event registration operations
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RegistrationService {
    
    /**
     * Register for an event (public)
     * @param eventId ID of the event
     * @param request Registration details
     * @return Registration response with QR code
     * @throws EventException if event not found or full
     * @throws RegistrationException if already registered
     */
    RegistrationResponse registerForEvent(Long eventId, RegistrationRequest request) 
            throws EventException, RegistrationException;
    
    /**
     * Confirm registration via email token
     * @param token Confirmation token
     * @return Registration response
     * @throws RegistrationException if token invalid or expired
     */
    RegistrationResponse confirmRegistration(String token) throws RegistrationException;
    
    /**
     * Cancel registration
     * @param registrationId Registration ID
     * @param email Email for verification
     * @param reason Cancellation reason
     * @throws RegistrationException if registration not found
     */
    void cancelRegistration(Long registrationId, String email, String reason) 
            throws RegistrationException;
    
    /**
     * Get registration by QR code
     * @param qrCode QR code string
     * @return Registration details
     * @throws RegistrationException if not found
     */
    RegistrationDetailsDto getRegistrationByQrCode(String qrCode) throws RegistrationException;
    
    /**
     * Check-in attendee
     * @param request Check-in request
     * @param userId ID of user performing check-in
     * @return Check-in response
     * @throws RegistrationException if check-in fails
     */
    CheckInResponse checkInAttendee(CheckInRequest request, Long userId) 
            throws RegistrationException;
    
    /**
     * Get all registrations for an event
     * @param eventId Event ID
     * @param pageable Pagination
     * @return Page of registrations
     */
    Page<RegistrationDetailsDto> getEventRegistrations(Long eventId, Pageable pageable);
    
    /**
     * Get checked-in attendees for an event
     * @param eventId Event ID
     * @return List of checked-in attendees
     */
    List<RegistrationDetailsDto> getCheckedInAttendees(Long eventId);
    
    /**
     * Export registrations to Excel
     * @param eventId Event ID
     * @return Excel file as byte array
     */
    byte[] exportRegistrationsToExcel(Long eventId) throws EventException;
    
    /**
     * Export registrations to PDF
     * @param eventId Event ID
     * @return PDF file as byte array
     */
    byte[] exportRegistrationsToPdf(Long eventId) throws EventException;
    
    /**
     * Send reminder to all registered attendees
     * @param eventId Event ID
     * @param userId User ID (for authorization)
     */
    void sendReminderEmails(Long eventId, Long userId) throws EventException;
    
    /**
     * Get registration statistics
     * @param eventId Event ID
     * @return Registration statistics
     */
    RegistrationStatistics getRegistrationStatistics(Long eventId);
    
    /**
     * Check if email is already registered for event
     * @param eventId Event ID
     * @param email Email to check
     * @return true if registered
     */
    boolean isEmailRegistered(Long eventId, String email);
    
    /**
     * Get waitlist position
     * @param registrationId Registration ID
     * @return Position in waitlist
     */
    int getWaitlistPosition(Long registrationId);

    /**
     * Get registration by ID with email verification
     * @param registrationId Registration ID
     * @param email Email for verification
     * @return Registration details with QR code image
     * @throws RegistrationException if not found or email mismatch
     */
    RegistrationDetailsDto getRegistrationById(Long registrationId, String email) 
            throws RegistrationException;
    
    /**
     * Count registrations by event and status
     * @param eventId Event ID
     * @param status Status string
     * @return Count of registrations
     */
    long countByEventAndStatus(Long eventId, String status);
}