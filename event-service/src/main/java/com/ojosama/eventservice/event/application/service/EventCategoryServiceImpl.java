package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    @Override
    public EventCategoryResult createCategory(CreateEventCategoryCommand command) {
        if (eventCategoryRepository.existsByName(command.name())) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }

        EventCategory category = EventCategory.builder()
            .name(command.name())
            .build();

        EventCategory saved = eventCategoryRepository.save(category);
        return EventCategoryResult.from(saved);
    }
}
