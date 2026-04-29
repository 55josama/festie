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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventCategoryCommandService {

    private final EventCategoryRepository eventCategoryRepository;

    public EventCategoryResult createCategory(CreateEventCategoryCommand command) {
        String normalizedName = EventCategory.normalizeName(command.name());
        if (eventCategoryRepository.existsByName(normalizedName)) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }
        EventCategory category = EventCategory.create(normalizedName);
        try {
            return EventCategoryResult.from(eventCategoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }
    }

    public EventCategoryResult updateCategory(UpdateEventCategoryCommand command) {
        EventCategory category = eventCategoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));
        String normalizedName = EventCategory.normalizeName(command.name());
        if (!category.getName().equals(normalizedName)
                && eventCategoryRepository.existsByNameExcludingId(normalizedName, command.categoryId())) {
            throw new EventException(EventErrorCode.EVENT_CATEGORY_ALREADY_EXISTS);
        }
        category.update(normalizedName);
        return EventCategoryResult.from(category);
    }

    public void deleteCategory(UUID userId, UUID categoryId) {
        EventCategory category = eventCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));
        category.deleted(userId);
    }
}
