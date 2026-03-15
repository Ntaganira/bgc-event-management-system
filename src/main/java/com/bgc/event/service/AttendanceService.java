package com.bgc.event.service;

import com.bgc.event.entity.Attendance;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;

import java.util.List;

public interface AttendanceService {
    Attendance markByQR(User user, String qrValue);

    Attendance markByCode(User user, Long eventId);

    List<Attendance> findByEvent(Event event);

    List<Attendance> findByUser(User user);

    boolean hasAttended(User user, Event event);

    long countByEvent(Event event);

    Attendance markByUserCode(String userCode, Long eventId);
}
