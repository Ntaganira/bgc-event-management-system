package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : AttendanceServiceImpl.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of Attendance Service
 * </pre>
 */

import com.bgc.event.dto.AttendanceDto;
import com.bgc.event.dto.AttendanceSummaryDto;
import com.bgc.event.dto.CheckInRequest;
import com.bgc.event.dto.CheckInResponse;
import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.Registration;
import com.bgc.event.exception.AttendanceException;
import com.bgc.event.exception.EventException;
import com.bgc.event.exception.RegistrationException;
import com.bgc.event.repository.AttendanceRepository;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.repository.RegistrationRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AttendanceService;
import com.bgc.event.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    @CacheEvict(value = {"attendance", "event-statistics"}, key = "#request.eventId")
    public CheckInResponse checkIn(CheckInRequest request, Long userId, HttpServletRequest httpRequest) 
            throws AttendanceException, RegistrationException, EventException {
        
        log.info("Processing check-in for event ID: {}, method: {}", request.getEventId(), request.getCheckInMethod());
        
        // Find registration
        Registration registration = findRegistration(request);
        
        // Validate check-in
        validateCheckIn(registration, request);
        
        // Check if already checked in
        if (attendanceRepository.existsByRegistrationId(registration.getId())) {
            Attendance existing = attendanceRepository.findByRegistrationId(registration.getId()).orElse(null);
            if (existing != null) {
                log.warn("Attendee already checked in at: {}", existing.getCheckedInAt());
                return CheckInResponse.builder()
                        .success(false)
                        .message("Already checked in at " + existing.getCheckedInAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                        .attendeeName(registration.getFirstName() + " " + registration.getLastName())
                        .email(registration.getEmail())
                        .checkInTime(existing.getCheckedInAt())
                        .build();
            }
        }
        
        // Get user info
        //User checkInUser = userId != null ? userRepository.findById(userId).orElse(null) : null;
        
        // Create attendance record
        Attendance attendance = Attendance.builder()
                .event(registration.getEvent())
                .registration(registration)
                .checkedInBy(userId)
                .checkInMethod(request.getCheckInMethod())
                .qrCodeUsed(request.getQrCode())
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(request.getDeviceInfo())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .notes(request.getNotes())
                .build();
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Update registration status
        registration.setCheckedIn(true);
        registration.setCheckedInAt(LocalDateTime.now());
        registration.setStatus(Registration.RegistrationStatus.ATTENDED);
        registrationRepository.save(registration);
        
        // Get total count
        long totalCheckedIn = attendanceRepository.countByEventId(request.getEventId());
        
        // Send WebSocket notification
        sendCheckInNotification(request.getEventId(), registration, totalCheckedIn);
        
        // Audit log
        auditService.logAction("CHECK_IN", userId, "ATTENDANCE", savedAttendance.getId(),
                registration.getFirstName() + " " + registration.getLastName(),
                null, null, httpRequest, "SUCCESS", null, null);
        
        log.info("Check-in successful for registration ID: {}", registration.getId());
        
        return CheckInResponse.builder()
                .success(true)
                .message("Check-in successful")
                .attendeeName(registration.getFirstName() + " " + registration.getLastName())
                .email(registration.getEmail())
                .checkInTime(savedAttendance.getCheckedInAt())
                .totalCheckedIn(totalCheckedIn)
                .build();
    }
    
    @Override
    public List<CheckInResponse> bulkCheckIn(List<CheckInRequest> requests, Long userId) {
        log.info("Processing bulk check-in for {} attendees", requests.size());
        
        List<CheckInResponse> responses = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (CheckInRequest request : requests) {
            try {
                CheckInResponse response = checkIn(request, userId, null);
                responses.add(response);
                if (response.isSuccess()) successCount++;
                else failCount++;
            } catch (Exception e) {
                log.error("Bulk check-in failed for request: {}", request, e);
                responses.add(CheckInResponse.builder()
                        .success(false)
                        .message("Error: " + e.getMessage())
                        .build());
                failCount++;
            }
        }
        
        auditService.logAction("BULK_CHECK_IN", userId, "ATTENDANCE", null,
                String.format("Success: %d, Failed: %d", successCount, failCount),
                null, null, null, "SUCCESS", null, null);
        
        return responses;
    }
    
    @Override
    @Transactional(readOnly = true)
    public AttendanceDto getAttendanceById(Long attendanceId) throws AttendanceException {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new AttendanceException("Attendance not found with ID: " + attendanceId));
        
        return mapToDto(attendance);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceDto> getEventAttendance(Long eventId, Pageable pageable) {
        Page<Attendance> attendances = attendanceRepository.findByEventId(eventId, pageable);
        return attendances.map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AttendanceDto getRegistrationAttendance(Long registrationId) throws AttendanceException {
        Attendance attendance = attendanceRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new AttendanceException("No attendance record found for registration ID: " + registrationId));
        
        return mapToDto(attendance);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "attendance-summary", key = "#eventId")
    public AttendanceSummaryDto getEventAttendanceSummary(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return AttendanceSummaryDto.builder().build();
        }
        
        long totalRegistrations = registrationRepository.countByEventId(eventId);
        long totalCheckedIn = attendanceRepository.countByEventId(eventId);
        List<Object[]> trend = attendanceRepository.getAttendanceTrend(eventId);
        List<Object[]> methods = attendanceRepository.getCheckInMethods(eventId);
        
        // Calculate check-in rate
        double checkInRate = totalRegistrations > 0 ? 
                (double) totalCheckedIn / totalRegistrations * 100 : 0;
        
        // Get peak check-in time
        String peakTime = getPeakCheckInTime(trend);
        
        // Method distribution
        Map<String, Long> methodDistribution = methods.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        
        return AttendanceSummaryDto.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .totalRegistrations((int) totalRegistrations)
                .totalCheckedIn((int) totalCheckedIn)
                .checkInRate(checkInRate)
                .remainingCheckIns((int) (totalRegistrations - totalCheckedIn))
                .checkInTrend(convertTrend(trend))
                .checkInMethods(methodDistribution)
                .peakCheckInTime(peakTime)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAttendanceTrends(Long eventId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Attendance> attendances = attendanceRepository.findByEventAndDateRange(eventId, startDate, endDate);
        
        Map<String, Long> dailyCounts = attendances.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCheckedInAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime current = startDate;
        
        while (!current.isAfter(endDate)) {
            String date = current.toLocalDate().toString();
            Map<String, Object> point = new HashMap<>();
            point.put("date", date);
            point.put("count", dailyCounts.getOrDefault(date, 0L));
            trend.add(point);
            current = current.plusDays(1);
        }
        
        return trend;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getCheckInMethodDistribution(Long eventId) {
        List<Object[]> methods = attendanceRepository.getCheckInMethods(eventId);
        return methods.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCurrentCheckInCount(Long eventId) {
        return attendanceRepository.countByEventId(eventId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCheckIn(Long registrationId) {
        return !attendanceRepository.existsByRegistrationId(registrationId);
    }
    
    @Override
    @CacheEvict(value = "attendance-summary", key = "#eventId")
    public void undoCheckIn(Long attendanceId, Long userId) throws AttendanceException {
        log.info("Undoing check-in for attendance ID: {}", attendanceId);
        
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new AttendanceException("Attendance not found with ID: " + attendanceId));
        
        Registration registration = attendance.getRegistration();
        
        // Update registration
        registration.setCheckedIn(false);
        registration.setCheckedInAt(null);
        registration.setStatus(Registration.RegistrationStatus.CONFIRMED);
        registrationRepository.save(registration);
        
        // Delete attendance
        attendanceRepository.delete(attendance);
        
        auditService.logAction("UNDO_CHECK_IN", userId, "ATTENDANCE", attendanceId,
                registration.getFirstName() + " " + registration.getLastName(),
                null, null, null, "SUCCESS", null, null);
        
        // Send WebSocket update
        sendCheckInNotification(attendance.getEvent().getId(), registration, 
                attendanceRepository.countByEventId(attendance.getEvent().getId()));
    }
    
    @Override
    public byte[] exportAttendanceReport(Long eventId, String format) throws EventException {
        // Implementation for PDF/Excel export
        // This would use the ReportExportService
        return new byte[0];
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getRecentCheckIns(Long eventId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        Page<Attendance> attendances = attendanceRepository.findByEventId(eventId, pageable);
        return attendances.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private Registration findRegistration(CheckInRequest request) throws RegistrationException {
        if (request.getRegistrationId() != null) {
            return registrationRepository.findById(request.getRegistrationId())
                    .orElseThrow(() -> new RegistrationException("Registration not found with ID: " + request.getRegistrationId()));
        } else if (request.getQrCode() != null) {
            return registrationRepository.findByQrCode(request.getQrCode())
                    .orElseThrow(() -> new RegistrationException("Invalid QR code"));
        } else {
            throw new RegistrationException("Either registrationId or qrCode must be provided");
        }
    }
    
    private void validateCheckIn(Registration registration, CheckInRequest request) 
            throws AttendanceException, EventException {
        
        Event event = registration.getEvent();
        
        // Verify event matches
        if (!event.getId().equals(request.getEventId())) {
            throw new AttendanceException("Registration does not belong to this event");
        }
        
        // Check if event is active
        if (event.getStatus() != Event.EventStatus.OPEN && 
            event.getStatus() != Event.EventStatus.FULL) {
            throw new AttendanceException("Event is not active for check-in");
        }
        
        // Check if registration is confirmed
        if (registration.getStatus() != Registration.RegistrationStatus.CONFIRMED && 
            registration.getStatus() != Registration.RegistrationStatus.ATTENDED) {
            throw new AttendanceException("Registration is not confirmed. Status: " + registration.getStatus());
        }
        
        // Check if already checked in (double-check)
        if (attendanceRepository.existsByRegistrationId(registration.getId())) {
            throw new AttendanceException("Already checked in");
        }
    }
    
    private void sendCheckInNotification(Long eventId, Registration registration, long totalCheckedIn) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHECK_IN");
        notification.put("eventId", eventId);
        notification.put("attendeeName", registration.getFirstName() + " " + registration.getLastName());
        notification.put("timestamp", LocalDateTime.now());
        notification.put("totalCheckedIn", totalCheckedIn);
        
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/checkins", (Object) notification);
    }
    
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
    
    private String getPeakCheckInTime(List<Object[]> trend) {
        if (trend.isEmpty()) return "N/A";
        
        // Find hour with most check-ins
        Map<Integer, Long> hourlyCounts = new HashMap<>();
        // for (Object[] data : trend) {
        //     // This would need proper parsing based on your data structure
        // }
        
        return hourlyCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> String.format("%02d:00", entry.getKey()))
                .orElse("N/A");
    }
    
    private List<Map<String, Object>> convertTrend(List<Object[]> trend) {
        return trend.stream()
                .map(arr -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", arr[0].toString());
                    point.put("count", arr[1]);
                    return point;
                })
                .collect(Collectors.toList());
    }
    
    private AttendanceDto mapToDto(Attendance attendance) {
        Registration reg = attendance.getRegistration();
        
        return AttendanceDto.builder()
                .id(attendance.getId())
                .eventId(attendance.getEvent().getId())
                .eventTitle(attendance.getEvent().getTitle())
                .registrationId(reg.getId())
                .attendeeName(reg.getFirstName() + " " + reg.getLastName())
                .email(reg.getEmail())
                .checkedInAt(attendance.getCheckedInAt())
                .checkedInBy(getUserName(attendance.getCheckedInBy()))
                .checkInMethod(attendance.getCheckInMethod())
                .ipAddress(attendance.getIpAddress())
                .deviceInfo(attendance.getDeviceInfo())
                .notes(attendance.getNotes())
                .build();
    }
    
    private String getUserName(Long userId) {
        if (userId == null) return "System";
        return userRepository.findById(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }
}