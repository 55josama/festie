package com.ojosama.chatservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ChatRoomLocationVerificationTrackerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ChatRoomLocationVerificationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new ChatRoomLocationVerificationTracker(redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void markVerified_shouldStoreVerificationFlagWithTtl() {
        UUID chatRoomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Duration ttl = Duration.ofMinutes(30);

        tracker.markVerified(chatRoomId, userId, ttl);

        verify(valueOperations).set(
                "chat:event:11111111-1111-1111-1111-111111111111:nearEvent:22222222-2222-2222-2222-222222222222",
                "1",
                ttl
        );
    }

    @Test
    void isVerified_shouldReadRedisPresence() {
        UUID chatRoomId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(redisTemplate.hasKey("chat:event:11111111-1111-1111-1111-111111111111:nearEvent:22222222-2222-2222-2222-222222222222"))
                .thenReturn(true);

        assertThat(tracker.isVerified(chatRoomId, userId)).isTrue();
    }
}
