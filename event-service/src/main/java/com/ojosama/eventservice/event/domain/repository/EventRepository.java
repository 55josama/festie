package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.Event;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
    List<Event> findAllByIds(List<UUID> ids);
    Page<Event> findAll(EventFilter filter, Pageable pageable);
    void delete(Event event);
}
