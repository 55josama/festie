package com.ojosama.eventservice.event.presentation.dto.response;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import java.util.UUID;

public record EventCategoryResponse(
    UUID id,
    String name
) {
    public static EventCategoryResponse from(EventCategoryResult result) {
        return new EventCategoryResponse(result.id(), result.name());
    }
}
