package com.ojosama.eventservice.eventrequest.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRequestQueryService {
    @Transactional(readOnly = true)
    public EventRequestResult getEventRequest(UUID requestId) {
        EventRequest request = eventRequestRepository.findById(requestId)
                .orElseThrow(() -> new EventRequestException(EventRequestErrorCode.EVENT_REQUEST_NOT_FOUND));
        return EventRequestResult.from(request);
    }
}
