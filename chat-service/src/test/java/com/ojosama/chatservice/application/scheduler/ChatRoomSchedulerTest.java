package com.ojosama.chatservice.application.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ojosama.chatservice.application.service.ChatRoomService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatRoomSchedulerTest {

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final Map<String, String> values = new HashMap<>();

    private ChatRoomScheduler scheduler;
    private String lockKey;

    @BeforeEach
    void setUp() {
        // Redis를 진짜로 붙이지 않고, 메모리 Map으로 락 상태만
        // 그래서 "락이 있으면 스킵 / 없으면 실행" 흐름만 테스트
        scheduler = new ChatRoomScheduler(chatRoomService, redisTemplate);
        lockKey = (String) ReflectionTestUtils.getField(ChatRoomScheduler.class, "LOCK_KEY");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0, String.class);
                    String token = invocation.getArgument(1, String.class);
                    if (values.containsKey(key)) {
                        return false;
                    }
                    values.put(key, token);
                    return true;
                });
        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return values.get(key);
        });
        when(redisTemplate.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return values.remove(key) != null;
        });
    }

    @Test
    void shouldRunScheduleWhenLockIsAvailable() {
        // 락이 비어 있으면 스케줄러가 정상적으로 실행되는거 확인
        when(chatRoomService.openScheduledChatRooms(any(LocalDateTime.class))).thenReturn(2);
        when(chatRoomService.closeScheduledChatRooms(any(LocalDateTime.class))).thenReturn(1);

        scheduler.syncChatRoomStatus();

        verify(chatRoomService).openScheduledChatRooms(any(LocalDateTime.class));
        verify(chatRoomService).closeScheduledChatRooms(any(LocalDateTime.class));
        assertThat(values).doesNotContainKey(lockKey);
    }

    @Test
    void shouldSkipScheduleWhenLockIsAlreadyHeld() {
        // 이미 다른 인스턴스가 락을 잡고 있으면 이번 실행은 바로 건너뜀
        values.put(lockKey, "other-token");

        scheduler.syncChatRoomStatus();

        verifyNoInteractions(chatRoomService);
        assertThat(values).containsKey(lockKey);
    }
}
