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

    @Override
    public Page<EventRequest> findAll(EventRequestFilter filter, Pageable pageable) {
        QEventRequest er = QEventRequest.eventRequest;
        BooleanBuilder where = buildWhere(er, filter);

        JPAQuery<EventRequest> query = queryFactory
                .selectFrom(er)
                .leftJoin(er.category).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        applySorting(query, er, pageable.getSort());

        List<EventRequest> content = query.fetch();
        long total = queryFactory
                .select(er.count())
                .from(er)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanBuilder buildWhere(QEventRequest er, EventRequestFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(er.deletedAt.isNull());

        if (filter.status() != null) {
            builder.and(er.status.eq(filter.status()));
        }
        if (filter.categoryName() != null) {
            builder.and(er.category.name.eq(filter.categoryName()));
        }
        if (filter.eventName() != null) {
            builder.and(er.eventName.containsIgnoreCase(filter.eventName()));
        }
        if (filter.requesterId() != null) {
            builder.and(er.requesterId.eq(filter.requesterId()));
        }
        if (filter.createdStart() != null) {
            builder.and(er.createdAt.goe(filter.createdStart()));
        }
        if (filter.createdEnd() != null) {
            builder.and(er.createdAt.loe(filter.createdEnd()));
        }
        return builder;
    }

    private void applySorting(JPAQuery<EventRequest> query, QEventRequest er, Sort sort) {
        if (sort.isUnsorted()) {
            query.orderBy(er.createdAt.desc());
            return;
        }
        sort.forEach(order -> {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "updatedAt" -> query.orderBy(asc ? er.updatedAt.asc() : er.updatedAt.desc());
                case "eventName" -> query.orderBy(asc ? er.eventName.asc() : er.eventName.desc());
                case "status" -> query.orderBy(asc ? er.status.asc() : er.status.desc());
                default -> query.orderBy(asc ? er.createdAt.asc() : er.createdAt.desc());
            }
        });
    }
}
