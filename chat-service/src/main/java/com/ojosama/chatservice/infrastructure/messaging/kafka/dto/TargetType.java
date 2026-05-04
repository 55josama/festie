package com.ojosama.chatservice.infrastructure.messaging.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {
    CHAT("채팅 메시지");

    private final String description;
}
