package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.event.payload.EventCreatedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventDeletedMessage;
import com.ojosama.eventservice.event.domain.event.payload.EventScheduleChangedMessage;
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
import com.ojosama.eventservice.event.domain.support.EventChanges;
import com.ojosama.eventservice.event.domain.support.EventSnapshot;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCommandService {
    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public EventResult createEvent(CreateEventCommand command) {
        EventCategory category = eventCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

        Event event = command.toEntity(category);

        if (command.schedules() == null || command.schedules().isEmpty()) {
            throw new EventException(EventErrorCode.VALIDATION_ERROR);
        }

        command.schedules().forEach(scheduleCommand ->
                event.addSchedule(scheduleCommand.toEntity(event)));

        Event saved = eventRepository.save(event);

        applicationEventPublisher.publishEvent(new EventCreatedMessage(
                saved.getId(), saved.getName(),
                saved.getCategory().getId(), saved.getCategory().getName(),
                saved.getEventTime().getStartAt(), saved.getEventTime().getEndAt()
        ));

        return EventResult.from(saved);
    }

    public EventResult updateEvent(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        EventCategory category = eventCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

        // 변경 전 상태 스냅샷 생성
        EventSnapshot beforeSnapshot = EventSnapshot.from(event);

        // Event 업데이트 수행
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

        // 행사 일정 업데이트
        boolean schedulesChanged = command.schedules() != null;
        if (schedulesChanged) {
            event.clearSchedules();
            command.schedules().forEach(s -> event.addSchedule(s.toEntity(event)));
        }

        // 변경 후 상태 스냅샷 생성
        EventSnapshot afterSnapshot = EventSnapshot.from(event);

        // 변경사항 추적
        EventChanges changes = EventSnapshot.compareSnapshots(beforeSnapshot, afterSnapshot);

        if (schedulesChanged || changes.hasChanges()) {
            applicationEventPublisher.publishEvent(EventScheduleChangedMessage.from(
                    event.getId(),
                    event.getName(),
                    changes.getChangedFields()
            ));
        }

        return EventResult.from(event);
    }

    public void deleteEvent(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        event.deleted(userId);
        eventRepository.delete(event);

        applicationEventPublisher.publishEvent(new EventDeletedMessage(event.getId(), event.getName()));
    }
}
