package com.ojosama.eventservice.event.infrastructure.persistence;

import com.ojosama.eventservice.event.domain.model.EventScheduleAction;
import com.ojosama.eventservice.event.domain.model.ScheduleActionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaEventScheduleActionRepository extends JpaRepository<EventScheduleAction, UUID> {
    @Query("SELECT a FROM EventScheduleAction a " +
           "WHERE a.status = :status AND a.scheduledAt <= :now " +
           "ORDER BY a.scheduledAt ASC, a.id ASC")
    List<EventScheduleAction> findPendingByScheduledAt(
            @Param("status") ScheduleActionStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
