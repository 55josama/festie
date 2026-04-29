package com.ojosama.eventservice.eventrequest.domain.repository;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import java.util.Optional;
import java.util.UUID;

public interface EventRequestRepository {
    EventRequest save(EventRequest request);

    Optional<EventRequest> findById(UUID id);
}
