package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventCategoryRepository extends JpaRepository<EventCategory, UUID> {
    boolean existsByNameAndDeletedAtIsNull(String name);
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, UUID id);
    List<EventCategory> findAllByDeletedAtIsNull();
    Optional<EventCategory> findByIdAndDeletedAtIsNull(UUID id);
    Optional<EventCategory> findByNameAndDeletedAtIsNull(String name);
}
