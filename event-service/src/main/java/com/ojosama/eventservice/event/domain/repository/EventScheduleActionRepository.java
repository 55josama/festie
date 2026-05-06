package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface EventScheduleActionRepository {
    EventScheduleAction save(EventScheduleAction action);
    List<EventScheduleAction> findPendingByScheduledAtBefore(LocalDateTime now, Pageable pageable);
}
