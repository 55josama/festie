package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.Event;

import java.util.Optional;

public interface EventRepository {
    Event save(Event event);
}
