package com.ojosama.eventservice.eventrequest.application.service;

import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import com.ojosama.eventservice.eventrequest.application.dto.command.CreateEventRequestCommand;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import com.ojosama.eventservice.eventrequest.domain.exception.EventRequestErrorCode;
import com.ojosama.eventservice.eventrequest.domain.exception.EventRequestException;
import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import java.util.UUID;
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

    public void cancelEventRequest(UUID userId, UUID requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        request.cancel(userId);
        eventRequestRepository.save(request);
    }

    public void adminCancelEventRequest(UUID adminId, UUID requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        request.adminCancel(adminId);
        eventRequestRepository.save(request);
    }

    public EventRequestResult approveEventRequest(UUID requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        request.approve();
        return EventRequestResult.from(eventRequestRepository.save(request));
    }

    public EventRequestResult rejectEventRequest(UUID requestId, String rejectReason) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        request.reject(rejectReason);
        return EventRequestResult.from(eventRequestRepository.save(request));
    }
}
