package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.model.vo.EventFee;
import com.ojosama.eventservice.event.domain.model.vo.EventLocation;
import com.ojosama.eventservice.event.domain.model.vo.EventTicketing;
import com.ojosama.eventservice.event.domain.model.vo.EventTime;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCommandServiceImpl implements EventCommandService {

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
    public EventResult updateEvent(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        EventCategory category = eventCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

        event.update(
                command.name(),
                category,
                new EventTime(command.startAt(), command.endAt()),
                new EventLocation(command.place(), command.latitude(), command.longitude()),
                new EventFee(command.minFee(), command.maxFee()),
                new EventTicketing(command.hasTicketing(), command.ticketingOpenAt(),
                        command.ticketingCloseAt(), command.ticketingLink()),
                command.officialLink(),
                command.description(),
                command.performer(),
                command.img()
        );

        if (command.schedules() != null) {
            event.clearSchedules();
            command.schedules().forEach(s -> event.addSchedule(s.toEntity(event)));
        }

        return EventResult.from(event);
    }
}
