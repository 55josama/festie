package com.ojosama.report.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventCategory {
    FESTIVAL("축제"),
    CONCERT("콘서트"),
    FANMEETING("팬미팅"),
    POPUPSTORE("팝업스토어");

    private final String description;
}
