package com.ojosama.chatservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatRoomPopularityTrackerTest {

    private static final String TOPIC_PREFIX = "/topic/rooms/";
    private static final String TOPIC_SUFFIX = "/messages";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final Map<String, Map<String, String>> hashes = new HashMap<>();
    private final Map<String, String> stringValues = new HashMap<>();
    private final Map<String, Double> scores = new HashMap<>();

    private ChatRoomPopularityTracker tracker;
    private String lockKeyPrefix;

    @BeforeEach
    void setUp() {
        // Redis 구조를 테스트용 메모리 Map으로 대체
        // 세션 락 / 구독 수 / 인기 점수만 집중해서 검증할 수 있다.
        tracker = new ChatRoomPopularityTracker(redisTemplate);
        lockKeyPrefix = (String) ReflectionTestUtils.getField(
                ChatRoomPopularityTracker.class,
                "SESSION_LOCK_KEY_PREFIX"
        );

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.delete(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            boolean removed = hashes.remove(key) != null;
            removed = stringValues.remove(key) != null || removed;
            return removed;
        });

        when(hashOperations.putIfAbsent(anyString(), any(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            String field = String.valueOf(invocation.getArgument(1, Object.class));
            String value = String.valueOf(invocation.getArgument(2, Object.class));
            Map<String, String> hash = hashes.computeIfAbsent(key, ignored -> new HashMap<>());
            return hash.putIfAbsent(field, value) == null;
        });

        when(hashOperations.get(anyString(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            String field = String.valueOf(invocation.getArgument(1, Object.class));
            Map<String, String> hash = hashes.get(key);
            return hash == null ? null : hash.get(field);
        });

        when(hashOperations.increment(anyString(), any(), anyLong())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            String field = String.valueOf(invocation.getArgument(1, Object.class));
            long delta = invocation.getArgument(2, Long.class);
            Map<String, String> hash = hashes.computeIfAbsent(key, ignored -> new HashMap<>());
            long current = parseLong(hash.get(field));
            long next = current + delta;
            hash.put(field, Long.toString(next));
            return next;
        });

        when(hashOperations.delete(anyString(), any())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            String field = String.valueOf(invocation.getArgument(1, Object.class));
            Map<String, String> hash = hashes.get(key);
            if (hash == null) {
                return 0L;
            }
            return hash.remove(field) != null ? 1L : 0L;
        });

        when(hashOperations.entries(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            Map<String, String> hash = hashes.get(key);
            if (hash == null) {
                return Collections.emptyMap();
            }
            return new HashMap<>(hash);
        });

        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble())).thenAnswer(invocation -> {
            String member = invocation.getArgument(1, String.class);
            double delta = invocation.getArgument(2, Double.class);
            double next = scores.getOrDefault(member, 0D) + delta;
            scores.put(member, next);
            return next;
        });

        when(zSetOperations.remove(anyString(), anyString())).thenAnswer(invocation -> {
            String member = invocation.getArgument(1, String.class);
            return scores.remove(member) == null ? 0L : 1L;
        });

        when(zSetOperations.score(anyString(), anyString())).thenAnswer(invocation -> {
            String member = invocation.getArgument(1, String.class);
            return scores.get(member);
        });

        when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).thenAnswer(invocation -> {
            List<Map.Entry<String, Double>> ordered = new ArrayList<>(scores.entrySet());
            ordered.sort(Map.Entry.<String, Double>comparingByValue().reversed());

            Set<ZSetOperations.TypedTuple<String>> tuples = new java.util.LinkedHashSet<>();
            ordered.forEach(entry -> {
                if (entry.getValue() != null && entry.getValue() > 0D) {
                    tuples.add(new DefaultTypedTuple<>(entry.getKey(), entry.getValue()));
                }
            });
            return tuples;
        });

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenAnswer(
                invocation -> {
                    String key = invocation.getArgument(0, String.class);
                    String value = invocation.getArgument(1, String.class);
                    if (stringValues.containsKey(key)) {
                        return false;
                    }
                    stringValues.put(key, value);
                    return true;
                });

        when(valueOperations.get(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            return stringValues.get(key);
        });
    }

    @Test
    void shouldCountOneViewerPerSessionForSameRoom() {
        // 같은 세션이 같은 방을 여러 번 구독해도 viewer는 1명으로만 세기.
        UUID roomId = UUID.randomUUID();

        tracker.markSubscribed("session-1", "sub-1", topic(roomId));
        tracker.markSubscribed("session-1", "sub-2", topic(roomId));
        tracker.markSubscribed("session-2", "sub-3", topic(roomId));

        assertThat(tracker.getViewerCount(roomId)).isEqualTo(2);

        tracker.markUnsubscribed("session-1", "sub-1");
        assertThat(tracker.getViewerCount(roomId)).isEqualTo(2);

        tracker.markUnsubscribed("session-1", "sub-2");
        assertThat(tracker.getViewerCount(roomId)).isEqualTo(1);

        tracker.clearSession("session-2");
        assertThat(tracker.getViewerCount(roomId)).isZero();
    }

    @Test
    void shouldSkipSessionUpdateWhenLockIsAlreadyHeld() {
        // 세션 락이 이미 있으면 이 세션의 구독/해제 처리는 건너뜀
        UUID roomId = UUID.randomUUID();
        String sessionId = "session-1";
        String sessionLockKey = sessionLockKey(sessionId);
        stringValues.put(sessionLockKey, "other-token");

        tracker.markSubscribed(sessionId, "sub-1", topic(roomId));

        assertThat(tracker.getViewerCount(roomId)).isZero();
        assertThat(hashes).isEmpty();
    }

    @Test
    void shouldReturnViewerCountsInDescendingOrder() {
        // Redis에 쌓인 인기 순서가 그대로 TOP 순서
        UUID room1 = UUID.randomUUID();
        UUID room2 = UUID.randomUUID();
        UUID room3 = UUID.randomUUID();

        tracker.markSubscribed("session-1", "sub-1", topic(room1));
        tracker.markSubscribed("session-2", "sub-2", topic(room1));
        tracker.markSubscribed("session-3", "sub-3", topic(room2));
        tracker.markSubscribed("session-4", "sub-4", topic(room2));
        tracker.markSubscribed("session-5", "sub-5", topic(room2));
        tracker.markSubscribed("session-6", "sub-6", topic(room3));

        Map<UUID, Integer> snapshot = tracker.snapshotViewerCounts();

        assertThat(snapshot.keySet()).containsExactly(room2, room1, room3);
        assertThat(snapshot.get(room2)).isEqualTo(3);
        assertThat(snapshot.get(room1)).isEqualTo(2);
        assertThat(snapshot.get(room3)).isEqualTo(1);
    }

    @Test
    void shouldIgnoreInvalidDestination() {
        // 채팅방 topic 형식이 아니면 집계에 넣지 않음
        tracker.markSubscribed("session-1", "sub-1", "/topic/not-a-room");
        tracker.markSubscribed("session-1", "sub-2", null);

        assertThat(tracker.snapshotViewerCounts()).isEmpty();
    }

    private String topic(UUID roomId) {
        return TOPIC_PREFIX + roomId + TOPIC_SUFFIX;
    }

    private String sessionLockKey(String sessionId) {
        return lockKeyPrefix.formatted(sessionId);
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return Long.parseLong(value);
    }
}
