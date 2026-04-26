package com.ojosama.eventservice.eventrequest.infrastructure.persistence;

import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRepository {

    private final JpaEventRequestRepository jpaEventRequestRepository;

}
