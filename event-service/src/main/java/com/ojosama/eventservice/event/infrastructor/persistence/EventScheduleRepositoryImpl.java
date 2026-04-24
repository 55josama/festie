package com.ojosama.eventservice.event.infrastructor.persistence;

import com.ojosama.eventservice.event.domain.repository.EventScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventScheduleRepositoryImpl implements EventScheduleRepository {

    private final JpaEventScheduleRepository jpaEventScheduleRepository;
}
