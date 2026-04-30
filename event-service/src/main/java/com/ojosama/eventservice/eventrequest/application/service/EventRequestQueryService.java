package com.ojosama.eventservice.eventrequest.application.service;

import com.ojosama.eventservice.eventrequest.application.dto.command.EventRequestListCommand;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import com.ojosama.eventservice.eventrequest.domain.exception.EventRequestErrorCode;
import com.ojosama.eventservice.eventrequest.domain.exception.EventRequestException;
import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestFilter;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventRequestQueryService {

    private final EventRequestRepository eventRequestRepository;

    @Transactional(readOnly = true)
    public Page<EventRequestResult> getEventRequests(EventRequestListCommand command, Pageable pageable) {
        validateDateRange(command);
        EventRequestFilter filter = new EventRequestFilter(
                command.status(),
                command.categoryName(),
                command.eventName(),
                command.requesterId(),
                command.createdStart(),
                command.createdEnd()
        );
        return eventRequestRepository.findAll(filter, pageable).map(EventRequestResult::from);
    }

    @Transactional(readOnly = true)
    public Page<EventRequestResult> getMyEventRequests(UUID requesterId, EventRequestListCommand command, Pageable pageable) {
        validateDateRange(command);
        EventRequestFilter filter = new EventRequestFilter(
                command.status(),
                command.categoryName(),
                command.eventName(),
                requesterId,
                command.createdStart(),
                command.createdEnd()
        );
        return eventRequestRepository.findAll(filter, pageable).map(EventRequestResult::from);
    }

    private void validateDateRange(EventRequestListCommand command) {
        if (command.createdStart() != null && command.createdEnd() != null
                && command.createdStart().isAfter(command.createdEnd())) {
            throw new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_INVALID_DATE_RANGE);
        }
    }

    @Transactional(readOnly = true)
    public EventRequestResult getEventRequest(UUID requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        return EventRequestResult.from(request);
    }
}
