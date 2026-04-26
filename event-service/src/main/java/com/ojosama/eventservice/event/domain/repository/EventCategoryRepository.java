package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventCategoryRepository {
    List<EventCategory> findAll();
    Optional<EventCategory> findById(UUID id);
    EventCategory save(EventCategory category);
    boolean existsByName(String name);
    boolean existsByNameExcludingId(String name, UUID id);
}
