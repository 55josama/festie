package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.result.EventCategoryResult;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.List;
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
        return eventCategoryRepository.findAll().stream()
                .map(EventCategoryResult::from)
                .toList();
    }
}
