package com.ojosama.eventservice.event.infrastructor.persistence;

import com.ojosama.eventservice.event.domain.model.EventSchedule;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEventScheduleRepository extends JpaRepository<EventSchedule, UUID> {
}
