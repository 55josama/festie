package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import java.util.List;

public interface EventCategoryQueryService {
    List<EventCategoryResult> getCategories();
}
