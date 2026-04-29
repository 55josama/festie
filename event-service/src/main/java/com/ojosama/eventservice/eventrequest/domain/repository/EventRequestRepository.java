package com.ojosama.eventservice.eventrequest.domain.repository;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRequestRepository {
    EventRequest save(EventRequest request);

    Optional<EventRequest> findById(UUID id);

    Page<EventRequest> findAll(EventRequestFilter filter, Pageable pageable);
}
