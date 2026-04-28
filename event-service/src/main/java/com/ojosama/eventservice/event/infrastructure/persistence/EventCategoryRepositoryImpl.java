package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventCategoryRepositoryImpl implements EventCategoryRepository {

    private final JpaEventCategoryRepository jpaEventCategoryRepository;

    @Override
    public List<EventCategory> findAll() {
        return jpaEventCategoryRepository.findAll();
    }

    @Override
    public List<EventCategory> findByDeletedAtIsNull() {
        return jpaEventCategoryRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public Optional<EventCategory> findById(UUID id) {
        return jpaEventCategoryRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public EventCategory save(EventCategory category) {
        return jpaEventCategoryRepository.save(category);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaEventCategoryRepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameExcludingId(String name, UUID id) {
        return jpaEventCategoryRepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }
}
