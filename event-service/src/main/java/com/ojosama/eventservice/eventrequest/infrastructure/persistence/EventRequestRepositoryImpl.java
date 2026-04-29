package com.ojosama.eventservice.eventrequest.infrastructure.persistence;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequest;
import com.ojosama.eventservice.eventrequest.domain.model.QEventRequest;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestFilter;
import com.ojosama.eventservice.eventrequest.domain.repository.EventRequestRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventRequestRepositoryImpl implements EventRequestRepository {

    private final JpaEventRequestRepository jpaEventRequestRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public EventRequest save(EventRequest request) {
        return jpaEventRequestRepository.save(request);
    }

    @Override
    public Optional<EventRequest> findById(UUID id) {
        return jpaEventRequestRepository.findByIdAndDeletedAtIsNull(id);
    }

    }
}
