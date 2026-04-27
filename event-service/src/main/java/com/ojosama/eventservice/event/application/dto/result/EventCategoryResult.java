package com.ojosama.eventservice.event.application.dto.result;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import java.util.UUID;

public record EventCategoryResult(
    UUID id,
    String name
) {
    public static EventCategoryResult from(EventCategory category) {
        return new EventCategoryResult(category.getId(), category.getName());
    }
}
