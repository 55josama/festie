package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.model.ScheduleActionStatus;
import com.ojosama.eventservice.event.domain.repository.EventScheduleActionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventScheduleActionRepositoryImpl implements EventScheduleActionRepository {

    private final JpaEventScheduleActionRepository jpaRepo;

    @Override
    public EventScheduleAction save(EventScheduleAction action) {
        return jpaRepo.save(action);
    }

    @Override
    public List<EventScheduleAction> findPendingByScheduledAtBefore(LocalDateTime now, Pageable pageable) {
        return jpaRepo.findPendingByScheduledAt(ScheduleActionStatus.PENDING, now, pageable);
    }

    @Override
    public List<EventScheduleAction> findPendingByEventId(UUID eventId) {
        return jpaRepo.findByEventIdAndStatus(eventId, ScheduleActionStatus.PENDING);
    }
}
