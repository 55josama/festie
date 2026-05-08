package com.ojosama.favoriteservice.domain.model;

import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventInfo {

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_start")
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "event_img")
    private String eventImg;

    @Column(name = "event_status")
    private EventStatus eventStatus;

    public EventInfo(UUID eventId, String eventName, String eventImg, LocalDateTime eventStart,
                     LocalDateTime eventEnd, String eventStatus) {
        validate(eventId, eventName);
        validateTime(eventStart, eventEnd);
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventImg = eventImg;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventStatus = EventStatus.valueOf(eventStatus);
        ;
    }

    private void validate(UUID eventId, String eventName) {
        if (eventId == null) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_ID);
        }
        if (eventName == null || eventName.isBlank()) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_NAME);
        }
    }

    private void validateTime(LocalDateTime eventStart, LocalDateTime eventEnd) {
        if (eventStart == null || eventEnd == null) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_DATE);
        }
    }
}
