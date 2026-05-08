package com.ojosama.chatservice.application.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomPopularityTracker {
    // 웹소켓 구독 주소에서 roomId만 꺼내려고 씀
    private static final Pattern ROOM_TOPIC_PATTERN =
            Pattern.compile("^/topic/rooms/([0-9a-fA-F-]{36})/messages$");

    // 전체 방 인원수를 Redis에 모아두는 칸
    private static final String POPULAR_ROOM_KEY = "chat:popular:rooms";
    // 이 세션이 어떤 구독을 했는지 적어두는 칸
    private static final String SESSION_SUBSCRIPTIONS_KEY_PREFIX = "chat:popular:sessions:%s:subs";
    // 이 세션이 방마다 몇 번 붙어있는지 적어두는 칸
    private static final String SESSION_ROOM_COUNTS_KEY_PREFIX = "chat:popular:sessions:%s:rooms";

    private final StringRedisTemplate redisTemplate;

    // 이벤트 리스너에서 호출 됨 (파라미터 값 전달되어서 옴)
    public void markSubscribed(String sessionId, String subscriptionId, String destination) {
        if (isBlank(sessionId) || isBlank(subscriptionId)) {
            return;
        }

        // 주소에서 roomId가 안 나오면 그냥 끝
        UUID roomId = extractRoomId(destination);
        if (roomId == null) {
            return;
        }

        // Redis에서 이 세션의 구독 기록부터 꺼냄
        // redisTemplate 에서 해시 전용 기능... hashOperations 생성
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String sessionSubscriptionsKey = sessionSubscriptionsKey(sessionId);
        String sessionRoomCountsKey = sessionRoomCountsKey(sessionId);
        String roomIdValue = roomId.toString();

        // 같은 subscription이 또 오면 중복으로 안 셈
        // putIfAbsent = 없을 때만 넣기, hashOperations 기능(Redis)
        // 성공하면 true, 이미 있으면 false 반환 -> false 면 종료
        Boolean inserted = hashOperations.putIfAbsent(sessionSubscriptionsKey, subscriptionId, roomIdValue);
        if (Boolean.FALSE.equals(inserted)) {
            return;
        }

        // 이 세션이 이 방에 몇 번 붙었는지 +1
        Long sessionRoomCount = hashOperations.increment(sessionRoomCountsKey, roomIdValue, 1L);
        // 처음 붙은 거면 전체 인기 숫자도 +1
        if (sessionRoomCount == 1L) {
            incrementGlobal(roomId);
        }
    }

    public void markUnsubscribed(String sessionId, String subscriptionId) {
        if (isBlank(sessionId) || isBlank(subscriptionId)) {
            return;
        }

        // 이 세션이 어떤 방을 보고 있었는지 먼저 확인
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String sessionSubscriptionsKey = sessionSubscriptionsKey(sessionId);
        String sessionRoomCountsKey = sessionRoomCountsKey(sessionId);

        // subscriptionId로 roomId를 찾음
        String roomIdValue = hashOperations.get(sessionSubscriptionsKey, subscriptionId);
        if (roomIdValue == null) {
            return;
        }

        // 구독 기록 하나 제거
        hashOperations.delete(sessionSubscriptionsKey, subscriptionId);

        // 이 세션에서 그 방을 하나 덜 보고 있다고 표시
        Long sessionRoomCount = hashOperations.increment(sessionRoomCountsKey, roomIdValue, -1L);
        // 이제 그 방을 완전히 안 보면 전체 숫자도 -1
        if (sessionRoomCount <= 0L) {
            hashOperations.delete(sessionRoomCountsKey, roomIdValue);
            decrementGlobal(UUID.fromString(roomIdValue));
        }

        // 세션에 남은 게 없으면 Redis 값도 지움
        cleanupSessionState(sessionSubscriptionsKey, sessionRoomCountsKey);
    }

    public void clearSession(String sessionId) {
        if (isBlank(sessionId)) {
            return;
        }

        // 연결이 끊기면 이 세션이 잡고 있던 방들을 전부 정리
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String sessionSubscriptionsKey = sessionSubscriptionsKey(sessionId);
        String sessionRoomCountsKey = sessionRoomCountsKey(sessionId);

        // 세션이 보고 있던 방들만큼 전체 인기 숫자에서 빼줌
        Map<String, String> sessionRoomCounts = hashOperations.entries(sessionRoomCountsKey);
        if (!sessionRoomCounts.isEmpty()) {
            sessionRoomCounts.forEach((roomIdValue, countValue) -> {
                if (toPositiveLong(countValue) > 0) {
                    decrementGlobal(UUID.fromString(roomIdValue));
                }
            });
        }

        redisTemplate.delete(sessionSubscriptionsKey);
        redisTemplate.delete(sessionRoomCountsKey);
    }

    public int getViewerCount(UUID roomId) {
        if (roomId == null) {
            return 0;
        }

        // 이 방 지금 몇 명인지 바로 확인
        Double score = redisTemplate.opsForZSet().score(POPULAR_ROOM_KEY, roomId.toString());
        return score == null ? 0 : Math.max(score.intValue(), 0);
    }

    public Map<UUID, Integer> snapshotViewerCounts() {
        // 인기 순서대로 전체 목록을 한 번 가져옴
        Set<ZSetOperations.TypedTuple<String>> topRooms =
                redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_ROOM_KEY, 0, -1); // 0,-1 전체조회 내림차순
        // 반환값이 Set<TypedTuple<String>>, roomId = String, score = Double 형태  -> 변환 필요

        if (topRooms == null || topRooms.isEmpty()) {
            return Map.of();
        }

        // Redis에서 가져온 순서 그대로 다시 담고, 타입 변환
        Map<UUID, Integer> snapshot = new LinkedHashMap<>();
        topRooms.forEach(tuple -> {
            if (tuple == null || tuple.getValue() == null || tuple.getScore() == null) {
                return;
            }

            try {
                UUID roomId = UUID.fromString(tuple.getValue()); // roomId = String -> UUID
                int count = Math.max(tuple.getScore().intValue(), 0); // score = Double -> int
                if (count > 0) {
                    snapshot.put(roomId, count); // 그리고나서 스냅샷에 넣기 (조회에 넘겨줄 값, Map<UUID, Integer>)
                }
            } catch (IllegalArgumentException ignored) {
                // 잘못된 roomId 값은 그냥 무시
            }
        });

        return snapshot;
    }

    private UUID extractRoomId(String destination) {
        if (isBlank(destination)) {
            return null;
        }

        // 우리가 쓰는 topic 형식인지 먼저 확인
        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination.trim());
        if (!matcher.matches()) {
            return null;
        }

        try {
            return UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void incrementGlobal(UUID roomId) {
        // 전체 인기 점수에 +1
        Double score = redisTemplate.opsForZSet().incrementScore(POPULAR_ROOM_KEY, roomId.toString(), 1D);
        if (score != null && score <= 0D) {
            redisTemplate.opsForZSet().remove(POPULAR_ROOM_KEY, roomId.toString());
        }
    }

    private void decrementGlobal(UUID roomId) {
        // 전체 인기 점수에 -1
        Double score = redisTemplate.opsForZSet().incrementScore(POPULAR_ROOM_KEY, roomId.toString(), -1D);
        if (score == null || score <= 0D) {
            redisTemplate.opsForZSet().remove(POPULAR_ROOM_KEY, roomId.toString());
        }
    }

    private void cleanupSessionState(String sessionSubscriptionsKey, String sessionRoomCountsKey) {
        // 세션에 남은 데이터가 없으면 아예 지움
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        if (hashOperations.entries(sessionSubscriptionsKey).isEmpty()) {
            redisTemplate.delete(sessionSubscriptionsKey);
        }
        if (hashOperations.entries(sessionRoomCountsKey).isEmpty()) {
            redisTemplate.delete(sessionRoomCountsKey);
        }
    }

    private String sessionSubscriptionsKey(String sessionId) {
        return SESSION_SUBSCRIPTIONS_KEY_PREFIX.formatted(sessionId);
    }

    private String sessionRoomCountsKey(String sessionId) {
        return SESSION_ROOM_COUNTS_KEY_PREFIX.formatted(sessionId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long toPositiveLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        try {
            return Math.max(Long.parseLong(value), 0L);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
