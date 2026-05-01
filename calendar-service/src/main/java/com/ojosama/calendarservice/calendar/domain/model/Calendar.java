package com.ojosama.calendarservice.calendar.domain.model;

import com.ojosama.calendarservice.calendar.domain.exception.CalendarErrorCode;
import com.ojosama.calendarservice.calendar.domain.exception.CalendarException;
import com.ojosama.common.audit.BaseUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    @Embedded
    private EventInfo eventInfo;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Builder(access = AccessLevel.PRIVATE)
    private Calendar(UUID userId, EventInfo eventInfo, String memo) {
        validateUserId(userId);
        validateMemo(memo);
        this.userId = userId;
        this.eventInfo = eventInfo;
        this.memo = memo;
    }

    public static Calendar create(UUID userId, String memo, EventInfo eventInfo) {
        return Calendar.builder()
                .userId(userId)
                .eventInfo(eventInfo)
                .memo(memo)
                .build();
    }

    public void updateMemo(String memo) {
        validateMemo(memo);
        this.memo = memo;
    }

    public void updateEventInfo(LocalDateTime newDate, String newName, LocalDateTime newTicketingDate) {
        if (newDate != null) eventInfo.updateEventDate(newDate);
        if (newName != null) eventInfo.updateEventName(newName);
        if (newTicketingDate != null) eventInfo.updateEventTicketingDate(newTicketingDate);
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }

    private void validateMemo(String memo) {
        if (memo != null && memo.length() > 1000) {
            throw new CalendarException(CalendarErrorCode.INVALID_INPUT);
        }
    }
}
