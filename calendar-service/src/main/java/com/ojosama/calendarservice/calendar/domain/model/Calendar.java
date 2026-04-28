package com.ojosama.calendarservice.calendar.domain.model;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.common.audit.BaseUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_calendar",
        indexes = {
                @Index(name = "idx_calendar_user_deleted", columnList = "user_id,deleted_at"),
                @Index(name = "idx_calendar_user_eventdate_deleted", columnList = "user_id,event_date,deleted_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_calendar_user_eventschedule_id",
                        columnNames = {"user_id", "event_schedule_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calendar extends BaseUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_schedule_id", nullable = false)
    private UUID eventScheduleId;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_ticketing_date")
    private LocalDateTime eventTicketingDate;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Builder(access = AccessLevel.PRIVATE)
    private Calendar(UUID userId, UUID eventScheduleId, LocalDateTime eventDate, String memo,
                     LocalDateTime eventTicketingDate) {
        validateUserId(userId);
        validateEventScheduleId(eventScheduleId);
        validateEventDate(eventDate);
        validateMemo(memo);
        this.userId = userId;
        this.eventScheduleId = eventScheduleId;
        this.eventDate = eventDate;
        this.memo = memo;
        this.eventTicketingDate = eventTicketingDate;
    }

    public static Calendar create(UUID userId, UUID eventScheduleId, LocalDateTime eventDate, String memo,
                                  LocalDateTime eventTicketingDate) {
        return Calendar.builder()
                .userId(userId)
                .eventScheduleId(eventScheduleId)
                .eventDate(eventDate)
                .memo(memo)
                .eventTicketingDate(eventTicketingDate)
                .build();
    }

    public void updateMemo(String memo) {
        validateMemo(memo);
        this.memo = memo;
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateEventScheduleId(UUID eventScheduleId) {
        if (eventScheduleId == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateMemo(String memo) {
        if (memo != null && memo.length() > 1000) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }
}
