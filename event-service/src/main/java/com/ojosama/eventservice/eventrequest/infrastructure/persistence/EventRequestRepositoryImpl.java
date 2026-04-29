package com.ojosama.eventservice.eventrequest.infrastructure.persistence;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRepository {

    private final JpaEventRequestRepository jpaEventRequestRepository;

    @Override
    public EventRequest save(EventRequest request) {
        return jpaEventRequestRepository.save(request);
    }

    @Override
    public Optional<EventRequest> findById(UUID id) {
        return jpaEventRequestRepository.findById(id);
    }
}
