package com.bgc.event.service;

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.EventDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;

import java.util.List;
import java.util.Optional;

public interface EventService {
    Event create(EventDto dto, User creator);

    Event update(Long id, EventDto dto);

    void delete(Long id);

    Optional<Event> findById(Long id);

    List<Event> findAll();

    List<Event> findUpcoming();

    List<CalendarEventDto> findAllAsCalendarEvents();

    long count();
}
