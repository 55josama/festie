package com.ojosama.eventservice.eventrequest.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ojosama.eventservice.eventrequest.application.dto.result.EventRequestResult;
import java.util.UUID;

public record EventRequestResponse(
        UUID id,
        @JsonProperty("requester_id") UUID requesterId,
        @JsonProperty("event_name") String eventName,
        @JsonProperty("category_id") UUID categoryId,
        String category,
        String link,
        String description,
        @JsonProperty("reject_reason") String rejectReason,
        String status
) {
    public static EventRequestResponse from(EventRequestResult result) {
        return new EventRequestResponse(
                result.id(),
                result.requesterId(),
                result.eventName(),
                result.categoryId(),
                result.categoryName(),
                result.link(),
                result.description(),
                result.rejectReason(),
                result.status()
        );
    }
}
