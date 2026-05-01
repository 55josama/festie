package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.Event;
import com.ojosama.eventservice.event.domain.model.QEvent;
import com.ojosama.eventservice.event.domain.repository.EventFilter;
import com.ojosama.eventservice.event.domain.repository.EventRepository;
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
public class EventRepositoryImpl implements EventRepository {

    private final JpaEventRepository jpaEventRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Event save(Event event) {
        return jpaEventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return jpaEventRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Page<Event> findAll(EventFilter filter, Pageable pageable) {
        QEvent event = QEvent.event;
        BooleanBuilder where = buildWhere(event, filter);

        JPAQuery<Event> query = queryFactory
            .selectFrom(event)
            .leftJoin(event.category).fetchJoin()
            .where(where)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        applySorting(query, event, pageable.getSort());

        List<Event> content = query.fetch();
        long total = queryFactory
            .select(event.count())
            .from(event)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Event> findAllActive() {
        return jpaEventRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public List<Event> findAllByIds(List<UUID> ids) {
        return jpaEventRepository.findAllByIdInAndDeletedAtIsNull(ids);
    }

    @Override
    public void delete(Event event) {
        jpaEventRepository.save(event);
    }

    private BooleanBuilder buildWhere(QEvent event, EventFilter filter) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(event.deletedAt.isNull());

        if (filter.categoryName() != null) {
            builder.and(event.category.name.eq(filter.categoryName()));
        }
        if (filter.status() != null) {
            builder.and(event.status.eq(filter.status()));
        }
        if (filter.startAt() != null) {
            builder.and(event.eventTime.startAt.goe(filter.startAt()));
        }
        if (filter.endAt() != null) {
            builder.and(event.eventTime.endAt.loe(filter.endAt()));
        }
        if (filter.year() != null) {
            builder.and(event.eventTime.startAt.year().eq(filter.year()));
        }
        if (filter.month() != null) {
            builder.and(event.eventTime.startAt.month().eq(filter.month()));
        }
        return builder;
    }

    private void applySorting(JPAQuery<Event> query, QEvent event, Sort sort) {
        if (sort.isUnsorted()) {
            query.orderBy(event.eventTime.startAt.asc());
            return;
        }
        sort.forEach(order -> {
            boolean asc = order.isAscending();
            switch (order.getProperty()) {
                case "name" -> query.orderBy(asc ? event.name.asc() : event.name.desc());
                case "createdAt" -> query.orderBy(asc ? event.createdAt.asc() : event.createdAt.desc());
                default -> query.orderBy(asc ? event.eventTime.startAt.asc() : event.eventTime.startAt.desc());
            }
        });
    }
}
