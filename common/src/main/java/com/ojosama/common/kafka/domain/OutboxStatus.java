package com.ojosama.common.kafka.domain;

public enum OutboxStatus {
    PENDING,    // 발행 대기
    SENT,       // Kafka 발행 성공
    FAILED      // 최대 재시도 초과 — 운영 알림 필요
}
