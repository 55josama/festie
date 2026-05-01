package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.EventListCommand;
import com.ojosama.eventservice.event.application.dto.result.EventDetailResult;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.repository.EventFilter;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
import com.ojosama.eventservice.event.infrastructure.client.ChatServiceClient;
import com.ojosama.eventservice.event.infrastructure.client.dto.ChatRoomSummaryDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eventRepository;
    private final ChatServiceClient chatServiceClient;

    @Transactional(readOnly = true)
    public Page<EventResult> getEvents(EventListCommand command, Pageable pageable) {
        EventFilter filter = new EventFilter(
                command.category(),
                command.status(),
                command.startAt(),
                command.endAt(),
                command.year(),
                command.month()
        );
        return eventRepository.findAll(filter, pageable).map(EventResult::from);
    }

    @Transactional(readOnly = true)
    public EventDetailResult getEventDetailById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        EventResult eventResult = EventResult.from(event);
        ChatRoomSummaryDto chatRoom = chatServiceClient.getChatRoomSummary(id).getData();
        return new EventDetailResult(eventResult, chatRoom != null ? chatRoom : ChatRoomSummaryDto.empty());
    }

    @Transactional(readOnly = true)
    public EventResult getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        return EventResult.from(event);
    }

    @Transactional(readOnly = true)
    public List<EventResult> getEventsByIds(List<UUID> ids) {
        return eventRepository.findAllByIds(ids).stream()
                .map(EventResult::from)
                .toList();
    }
}
