package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.repository.EventCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventCategoryRepositoryImpl implements EventCategoryRepository {

    private final JpaEventCategoryRepository jpaEventCategoryRepository;
}
