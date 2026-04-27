package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.Event;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
}
