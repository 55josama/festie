package com.ojosama.eventservice.event.domain.model;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.eventservice.event.domain.exception.EventErrorCode;
import com.ojosama.eventservice.event.domain.exception.EventException;
import com.ojosama.eventservice.event.domain.model.vo.ScheduleTime;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_event_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Embedded
    private ScheduleTime scheduleTime;

    @Builder
    public EventSchedule(Event event, String name, ScheduleTime scheduleTime) {
        validateEvent(event);
        validateScheduleName(name);

        this.event = event;
        this.name = name;
        this.scheduleTime = scheduleTime;
    }

    private void validateEvent(Event event) {
        if (event == null) {
            throw new EventException(EventErrorCode.EVENT_NOT_FOUND);
        }
    }

    private void validateScheduleName(String name) {
        if (name == null || name.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
        if (name.length() > 100) {
            throw new EventException(EventErrorCode.EVENT_SCHEDULE_INVALID_TIME);
        }
    }
}
