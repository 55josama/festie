package com.ojosama.favoriteservice.domain.model;

import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

    @Column(name = "event_img")
    private String eventImg;

    public EventInfo(UUID eventId, String eventName, String eventImg) {
        validate(eventId, eventName);
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventImg = eventImg;
    }

    private void validate(UUID eventId, String eventName) {
        if (eventId == null) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_ID);
        }
        if (eventName == null || eventName.isBlank()) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_EVENT_NAME);
        }
    }
}
