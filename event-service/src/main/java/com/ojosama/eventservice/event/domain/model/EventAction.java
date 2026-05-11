package com.ojosama.eventservice.event.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventAction {
    MARK_IN_PROGRESS("시작"),
    MARK_COMPLETED("종료");

    private final String description;
}
