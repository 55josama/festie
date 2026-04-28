package com.ojosama.notificationservice.domain.model.notification;

import lombok.Getter;

@Getter
public enum Target {
    EVENT("행사"),
    OPERATION("운영"),
    COMMUNITY("커뮤니티");

    private final String description;

    Target(String description) {
        this.description = description;
    }

}
