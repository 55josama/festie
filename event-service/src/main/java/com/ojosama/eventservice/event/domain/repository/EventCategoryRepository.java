package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import java.util.Optional;
import java.util.UUID;

public interface EventCategoryRepository {
    Optional<EventCategory> findById(UUID id);
    EventCategory save(EventCategory category);
    boolean existsByName(String name);
}
