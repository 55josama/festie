package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCategoryCommand;
import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCategoryCommandServiceImpl implements EventCategoryCommandService {

    private final EventCategoryRepository eventCategoryRepository;

    @Override
    public EventCategoryResult createCategory(CreateEventCategoryCommand command) {
        if (eventCategoryRepository.existsByName(command.name())) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }
        EventCategory category = EventCategory.create(command.name());
        return EventCategoryResult.from(eventCategoryRepository.save(category));
    }

    @Override
    public EventCategoryResult updateCategory(UpdateEventCategoryCommand command) {
        EventCategory category = eventCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));
        if (eventCategoryRepository.existsByName(command.name())) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }
        category.update(command.name());
        return EventCategoryResult.from(category);
    }

    @Override
    public void deleteCategory(UUID userId, UUID categoryId) {
        EventCategory category = eventCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));
        category.deleted(userId);
    }
}
