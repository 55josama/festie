package com.ojosama.favoriteservice.domain.model;

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

}
