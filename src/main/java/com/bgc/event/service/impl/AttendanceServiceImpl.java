package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : AttendanceServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Attendance.AttendanceMethod;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.repository.AttendanceRepository;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;

    @Override
    public Attendance markByQR(User user, String qrValue) {
        Event event = eventRepository.findAll().stream()
            .filter(e -> qrValue.equals(e.getQrCodeValue()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Invalid QR code"));
        return markAttendance(user, event, AttendanceMethod.QR);
    }

    @Override
    public Attendance markByCode(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        return markAttendance(user, event, AttendanceMethod.CODE);
    }

    private Attendance markAttendance(User user, Event event, AttendanceMethod method) {
        if (attendanceRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("Already checked in for this event");
        }
        Attendance att = Attendance.builder()
            .user(user)
            .event(event)
            .method(method)
            .build();
        return attendanceRepository.save(att);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findByEvent(Event event) {
        return attendanceRepository.findByEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findByUser(User user) {
        return attendanceRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttended(User user, Event event) {
        return attendanceRepository.existsByUserAndEvent(user, event);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByEvent(Event event) {
        return attendanceRepository.countByEvent(event);
    }
}
