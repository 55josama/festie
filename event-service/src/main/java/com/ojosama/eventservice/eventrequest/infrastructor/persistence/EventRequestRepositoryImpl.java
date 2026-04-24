package com.ojosama.eventservice.eventrequest.infrastructor.persistence;

import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRepository {

    private final JpaEventRequestRespository jpaEventRequestRepository;
}
