package com.ojosama.eventservice.eventrequest.application.dto.result;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import java.util.UUID;

public record EventRequestResult(
        UUID id,
        UUID requesterId,
        String eventName,
        UUID categoryId,
        String categoryName,
        String link,
        String description,
        String rejectReason,
        String status
) {
    public static EventRequestResult from(EventRequest request) {
        return new EventRequestResult(
                request.getId(),
                request.getRequesterId(),
                request.getEventName(),
                request.getCategory().getId(),
                request.getCategory().getName(),
                request.getLink(),
                request.getDescription(),
                request.getRejectReason(),
                request.getStatus().name()
        );
    }
}
