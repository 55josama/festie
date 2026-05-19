package com.ojosama.chatservice.infrastructure.persistence;

import com.ojosama.chatservice.domain.model.ChatRoom;
import com.ojosama.chatservice.domain.model.ChatRoomStatus;
import com.ojosama.chatservice.domain.repository.ChatRoomRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final EntityManager entityManager;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomJpaRepository.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(UUID id) {
        return chatRoomJpaRepository.findById(id);
    }

    @Override
    public Optional<ChatRoom> findByEventId(UUID eventId) {
        return chatRoomJpaRepository.findByEventId(eventId);
    }

    @Override
    public Page<ChatRoom> findAll(Pageable pageable) {
        return chatRoomJpaRepository.findAll(pageable);
    }

    @Override
    public List<ChatRoom> findAllByIds(Collection<UUID> ids) {
        return chatRoomJpaRepository.findAllById(ids);
    }

    @Override
    public List<ChatRoom> findAllByStatus(ChatRoomStatus status) {
        return chatRoomJpaRepository.findAllByStatus(status);
    }

    @Override
    public Page<ChatRoom> findAllFiltered(ChatRoomStatus status, LocalDateTime scheduledOpenAtFrom, LocalDateTime scheduledOpenAtTo, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ChatRoom> contentQuery = cb.createQuery(ChatRoom.class);
        Root<ChatRoom> contentRoot = contentQuery.from(ChatRoom.class);
        List<Predicate> predicates = buildFilteredPredicates(cb, contentRoot, status, scheduledOpenAtFrom, scheduledOpenAtTo);
        contentQuery.where(predicates.toArray(Predicate[]::new));
        applySort(cb, contentQuery, contentRoot, pageable.getSort());

        TypedQuery<ChatRoom> typedQuery = entityManager.createQuery(contentQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<ChatRoom> content = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ChatRoom> countRoot = countQuery.from(ChatRoom.class);
        List<Predicate> countPredicates = buildFilteredPredicates(cb, countRoot, status, scheduledOpenAtFrom, scheduledOpenAtTo);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(Predicate[]::new));

        Long total = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ChatRoom> findScheduledToOpen(LocalDateTime now) {
        return chatRoomJpaRepository.findScheduledToOpen(now);
    }

    @Override
    public List<ChatRoom> findScheduledToClose(LocalDateTime now) {
        return chatRoomJpaRepository.findScheduledToClose(now);
    }

    private List<Predicate> buildFilteredPredicates(
            CriteriaBuilder cb,
            Root<ChatRoom> root,
            ChatRoomStatus status,
            LocalDateTime scheduledOpenAtFrom,
            LocalDateTime scheduledOpenAtTo
    ) {
        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (scheduledOpenAtFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("schedule").get("scheduledOpenAt"), scheduledOpenAtFrom));
        }
        if (scheduledOpenAtTo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("schedule").get("scheduledOpenAt"), scheduledOpenAtTo));
        }
        return predicates;
    }

    private void applySort(CriteriaBuilder cb, CriteriaQuery<ChatRoom> query, Root<ChatRoom> root, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }

        List<Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            jakarta.persistence.criteria.Path<?> path = resolvePath(root, order.getProperty());
            orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        query.orderBy(orders);
    }

    private jakarta.persistence.criteria.Path<?> resolvePath(Root<ChatRoom> root, String propertyPath) {
        String[] segments = propertyPath.split("\\.");
        jakarta.persistence.criteria.Path<?> path = root.get(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            path = path.get(segments[i]);
        }
        return path;
    }
}
