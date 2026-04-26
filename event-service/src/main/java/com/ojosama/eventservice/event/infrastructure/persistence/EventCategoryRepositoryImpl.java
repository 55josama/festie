package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventCategoryRepositoryImpl implements EventCategoryRepository {

    private final JpaEventCategoryRepository jpaEventCategoryRepository;

    @Override
    public Optional<EventCategory> findById(UUID id) {
        return jpaEventCategoryRepository.findById(id);
    }

    @Override
    public EventCategory save(EventCategory category) {
        return jpaEventCategoryRepository.save(category);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaEventCategoryRepository.existsByName(name);
    }
}
