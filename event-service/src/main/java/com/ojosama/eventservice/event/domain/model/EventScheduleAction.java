package com.ojosama.eventservice.event.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "p_event_schedule_actions",
    indexes = {
        @Index(name = "idx_schedule_status_scheduled_at", columnList = "status, scheduled_at"),
        @Index(name = "idx_schedule_event_id", columnList = "event_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventScheduleAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventAction action;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleActionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(length = 500)
    private String errorMessage;

    public void markExecuted(LocalDateTime executedAt) {
        this.status = ScheduleActionStatus.EXECUTED;
        this.executedAt = executedAt;
    }

    public void markFailed(String errorMessage) {
        this.status = ScheduleActionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
