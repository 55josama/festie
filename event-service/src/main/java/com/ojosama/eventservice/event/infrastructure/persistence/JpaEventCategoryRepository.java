package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventCategory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventCategoryRepository extends JpaRepository<EventCategory, UUID> {
}
