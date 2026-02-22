package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : RegistrationService.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for event registration operations
 * </pre>
 */

import com.bgc.event.dto.*;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RegistrationService {
    
    /**
     * Register for an event (public)
     */
    RegistrationResponse registerForEvent(Long eventId, RegistrationRequest request) 
            throws EventException, RegistrationException;
    
    /**
     * Confirm registration via email token
     */
    RegistrationResponse confirmRegistration(String token) throws RegistrationException;
    
    /**
     * Cancel registration
     */
    void cancelRegistration(Long registrationId, String email, String reason) 
            throws RegistrationException;
    
    /**
     * Get registration by QR code
     */
    RegistrationDetailsDto getRegistrationByQrCode(String qrCode) throws RegistrationException;
    
    /**
     * Check-in attendee
     */
    CheckInResponse checkInAttendee(CheckInRequest request, Long userId) 
            throws RegistrationException;
    
    /**
     * Get all registrations for an event
     */
    Page<RegistrationDetailsDto> getEventRegistrations(Long eventId, Pageable pageable);
    
    /**
     * Get checked-in attendees for an event
     */
    List<RegistrationDetailsDto> getCheckedInAttendees(Long eventId);
    
    /**
     * Export registrations to Excel
     */
    byte[] exportRegistrationsToExcel(Long eventId) throws EventException;
    
    /**
     * Export registrations to PDF
     */
    byte[] exportRegistrationsToPdf(Long eventId) throws EventException;
    
    /**
     * Send reminder to all registered attendees
     */
    void sendReminderEmails(Long eventId, Long userId) throws EventException;
    
    /**
     * Get registration statistics
     */
    RegistrationStatistics getRegistrationStatistics(Long eventId);
    
    /**
     * Check if email is already registered for event
     */
    boolean isEmailRegistered(Long eventId, String email);
    
    /**
     * Get waitlist position
     */
    int getWaitlistPosition(Long registrationId);
    
    /**
     * Get registration by ID with email verification
     */
    RegistrationDetailsDto getRegistrationById(Long registrationId, String email) 
            throws RegistrationException;
    
    /**
     * Count registrations by event and status
     */
    long countByEventAndStatus(Long eventId, String status);
    
    // ==================== NEW METHODS FOR ANALYTICS ====================
    
    /**
     * Get monthly registration statistics for dashboard charts
     * @param startDate Start date
     * @param endDate End date
     * @return List of Object arrays containing [month, approvedCount, pendingCount]
     */
    List<Object[]> getMonthlyRegistrationStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get registration status distribution
     * @return Map of status to count
     */
    Map<String, Long> getRegistrationStatusDistribution();
    
    /**
     * Get daily registration trend
     * @param eventId Optional event ID filter
     * @param days Number of days
     * @return List of Object arrays containing [date, count]
     */
    List<Object[]> getDailyRegistrationTrend(Long eventId, int days);
    
    /**
     * Get hourly registration distribution
     * @param eventId Optional event ID filter
     * @return Map of hour to count
     */
    Map<Integer, Long> getHourlyRegistrationDistribution(Long eventId);
    
    /**
     * Get registration counts by date range
     * @param startDate Start date
     * @param endDate End date
     * @return Total registrations in date range
     */
    long getRegistrationCountBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get top organizations by registration count
     * @param limit Number of results
     * @return List of Object arrays containing [organization, count]
     */
    List<Object[]> getTopOrganizations(int limit);
    
    /**
     * Get registration conversion rate (confirmed vs total)
     * @param eventId Event ID
     * @return Conversion rate percentage
     */
    double getRegistrationConversionRate(Long eventId);
    
    /**
     * Get average registrations per day
     * @param eventId Optional event ID
     * @return Average daily registrations
     */
    double getAverageDailyRegistrations(Long eventId);
    
    /**
     * Get peak registration hour
     * @param eventId Optional event ID
     * @return Hour of day (0-23)
     */
    int getPeakRegistrationHour(Long eventId);
    
    /**
     * Get registration growth rate
     * @param period Days to compare (30, 60, 90)
     * @return Growth rate percentage
     */
    double getRegistrationGrowthRate(int period);
}