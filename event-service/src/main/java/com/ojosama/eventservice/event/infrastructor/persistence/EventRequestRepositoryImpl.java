package com.ojosama.eventservice.event.infrastructor.persistence;

import com.ojosama.eventservice.event.domain.repository.EventRequestRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRespository {

    private final JpaEventRequestRespository jpaEventRequestRepository;
}
