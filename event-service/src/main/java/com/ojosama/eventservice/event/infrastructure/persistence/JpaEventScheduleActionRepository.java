package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventScheduleActionRepository extends JpaRepository<EventScheduleAction, UUID> {
}
