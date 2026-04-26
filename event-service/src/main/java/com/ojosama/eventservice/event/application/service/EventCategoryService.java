package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;

public interface EventCategoryService {
    EventCategoryResult createCategory(CreateEventCategoryCommand command);
}
