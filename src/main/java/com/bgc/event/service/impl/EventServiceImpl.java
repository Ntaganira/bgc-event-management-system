package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : EventServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.dto.CalendarEventDto;
import com.bgc.event.dto.EventDto;
import com.bgc.event.entity.Event;
import com.bgc.event.entity.User;
import com.bgc.event.repository.EventRepository;
import com.bgc.event.service.EventService;
import lombok.RequiredArgsConstructor;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private static final String[] COLORS = {"#3b82f6","#22c55e","#f59e0b","#8b5cf6","#ef4444","#06b6d4"};

    @Override
    public Event create(EventDto dto, User creator) {
        Event event = Event.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .location(dto.getLocation())
            .startDateTime(dto.getStartDateTime())
            .endDateTime(dto.getEndDateTime())
            .qrCodeValue(UUID.randomUUID().toString())
            .createdBy(creator)
            .build();
        return eventRepository.save(event);
    }

    @Override
    public Event update(Long id, EventDto dto) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        return eventRepository.save(event);
    }

    @Override
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(Long id) {
        Optional<Event> eventOptional = eventRepository.findById(id);
        eventOptional.ifPresent(item -> Hibernate.initialize(item.getAttendanceRecords()));
        return eventOptional;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
         List<Event> events = eventRepository.findAll();
         events.forEach(event -> Hibernate.initialize(event.getAttendanceRecords()));
         return events;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findUpcoming() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventDto> findAllAsCalendarEvents() {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<Event> events = eventRepository.findAll();
        return events.stream()
            .map(e -> CalendarEventDto.builder()
                .id(String.valueOf(e.getId()))
                .title(e.getTitle())
                .start(e.getStartDateTime().format(fmt))
                .end(e.getEndDateTime().format(fmt))
                .color(COLORS[(int)(e.getId() % COLORS.length)])
                .extendedPropsLocation(e.getLocation())
                .url("/events/" + e.getId())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return eventRepository.count();
    }
}
