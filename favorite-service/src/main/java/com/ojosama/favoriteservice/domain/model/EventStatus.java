package com.ojosama.favoriteservice.domain.model;

import com.ojosama.favoriteservice.domain.exception.FavoriteErrorCode;
import com.ojosama.favoriteservice.domain.exception.FavoriteException;
import lombok.Getter;

@Getter
public enum EventStatus {
    SCHEDULED("예정"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    public static EventStatus from(String status) {
        try {
            return EventStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new FavoriteException(FavoriteErrorCode.INVALID_MESSAGE_PAYLOAD);
        }
    }
}
