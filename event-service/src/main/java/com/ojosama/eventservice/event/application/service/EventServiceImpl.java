package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;

    @Override
    public EventResult createEvent(CreateEventCommand command) {
        EventCategory category = eventCategoryRepository.findById(command.categoryId())
            .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

        Event event = command.toEntity(category);

        command.schedules().forEach(scheduleCommand ->
            event.addSchedule(scheduleCommand.toEntity(event)));

        Event saved = eventRepository.save(event);
        return EventResult.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResult getEventById(UUID id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        return EventResult.from(event);
    }
}
