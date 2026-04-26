package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import java.util.List;
import java.util.UUID;

public interface EventCategoryQueryService {
    List<EventCategoryResult> getCategories();
    EventCategoryResult getCategoryById(UUID id);
}
