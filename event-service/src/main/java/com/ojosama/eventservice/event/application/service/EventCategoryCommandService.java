package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import java.util.UUID;

public interface EventCategoryCommandService {
    EventCategoryResult createCategory(CreateEventCategoryCommand command);
    EventCategoryResult updateCategory(UpdateEventCategoryCommand command);
    void deleteCategory(UUID userId, UUID categoryId);
}
