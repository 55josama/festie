package com.ojosama.eventservice.eventrequest.application.service;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import com.ojosama.eventservice.eventrequest.application.dto.command.CreateEventRequestCommand;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventRequestCommandService {

    private final EventRequestRepository eventRequestRepository;
    private final EventCategoryRepository eventCategoryRepository;

    public EventRequestResult createEventRequest(CreateEventRequestCommand command) {
        EventCategory category = eventCategoryRepository.findByName(command.categoryName())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));

        EventRequest request = EventRequest.create(
                command.requesterId(),
                command.eventName(),
                category,
                command.link(),
                command.description()
        );

        EventRequest saved = eventRequestRepository.save(request);
        return EventRequestResult.from(saved);
    }
}
