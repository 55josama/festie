package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventCategoryQueryServiceImpl implements EventCategoryQueryService {

    private final EventCategoryRepository eventCategoryRepository;

    @Override
    public List<EventCategoryResult> getCategories() {
        return eventCategoryRepository.findByDeletedAtIsNull().stream()
                .map(EventCategoryResult::from)
                .toList();
    }

    @Override
    public EventCategoryResult getCategoryById(UUID id) {
        return eventCategoryRepository.findById(id)
                .map(EventCategoryResult::from)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_CATEGORY_NOT_FOUND));
    }
}
