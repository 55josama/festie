package com.ojosama.blacklist.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationType {
    MANUAL("수동 등록"),
    AUTOMATIC("자동 등록");

    private final String description;
}
