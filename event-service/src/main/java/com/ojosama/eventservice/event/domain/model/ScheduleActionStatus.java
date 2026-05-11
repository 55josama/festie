package com.ojosama.eventservice.event.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleActionStatus {
    PENDING("대기"),
    EXECUTED("실행됨"),
    FAILED("실패");

    private final String description;
}
