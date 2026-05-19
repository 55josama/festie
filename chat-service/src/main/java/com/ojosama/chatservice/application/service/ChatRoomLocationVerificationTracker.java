package com.ojosama.chatservice.application.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomLocationVerificationTracker {

    private static final String KEY_PREFIX = "chat:event:";
    private static final String KEY_SUFFIX = ":nearEvent:";
    private static final long DEFAULT_TTL_MINUTES = 60L;

    private final StringRedisTemplate redisTemplate;

    public void markVerified(UUID eventId, UUID userId, Duration ttl) {
        if (eventId == null || userId == null) {
            return;
        }

        Duration safeTtl = normalizeTtl(ttl);
        try {
            redisTemplate.opsForValue().set(key(eventId, userId), "1", safeTtl);
        } catch (Exception ignored) {
            // 위치 배지 저장 실패는 핵심 채팅 기능을 막지 않음
        }
    }

    public boolean isVerified(UUID eventId, UUID userId) {
        if (eventId == null || userId == null) {
            return false;
        }
        try {
            Boolean hasKey = redisTemplate.hasKey(key(eventId, userId));
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception ignored) {
            return false;
        }
    }

    private Duration normalizeTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return Duration.ofMinutes(DEFAULT_TTL_MINUTES);
        }
        return ttl;
    }

    private String key(UUID eventId, UUID userId) {
        return KEY_PREFIX + eventId + KEY_SUFFIX + userId;
    }
}
