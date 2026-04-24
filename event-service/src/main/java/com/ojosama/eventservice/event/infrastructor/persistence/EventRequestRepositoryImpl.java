package com.ojosama.eventservice.event.infrastructor.persistence;

import com.ojosama.eventservice.event.domain.repository.EventRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRepository {

    private final JpaEventRequestRespository jpaEventRequestRepository;
}
