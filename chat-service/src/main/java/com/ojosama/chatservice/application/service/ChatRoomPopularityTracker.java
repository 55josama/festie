package com.ojosama.chatservice.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomPopularityTracker {
    // /topic/rooms/{roomId}/messages 에서 roomId만 뽑기
    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("^/topic/rooms/([0-9a-fA-F-]{36})/messages$");

    // 방별 지금 접속자 수
    private final ConcurrentMap<UUID, AtomicInteger> roomViewerCounts = new ConcurrentHashMap<>();
    // 세션이 어떤 방을 어떤 구독으로 보고 있는지
    private final ConcurrentMap<String, ConcurrentMap<String, UUID>> subscriptionsBySession = new ConcurrentHashMap<>();
    // 세션 안에서 방별 구독 횟수
    private final ConcurrentMap<String, ConcurrentMap<UUID, AtomicInteger>> roomCountsBySession = new ConcurrentHashMap<>();

    // 구독 시작
    public void markSubscribed(String sessionId, String subscriptionId, String destination) {
        if (isBlank(sessionId) || isBlank(subscriptionId)) {
            return;
        }

        // destination 에서 roomId를 꺼냄
        UUID roomId = extractRoomId(destination);
        if (roomId == null) {
            return;
        }

        // 이 세션의 구독 목록 저장
        ConcurrentMap<String, UUID> sessionSubscriptions = subscriptionsBySession.computeIfAbsent(
                sessionId,
                key -> new ConcurrentHashMap<>()
        );
        if (sessionSubscriptions.putIfAbsent(subscriptionId, roomId) != null) {
            return;
        }

        // 이 세션이 이 방을 몇 번 보고 있는지 저장
        ConcurrentMap<UUID, AtomicInteger> sessionRoomCounts = roomCountsBySession.computeIfAbsent(
                sessionId,
                key -> new ConcurrentHashMap<>()
        );
        AtomicInteger sessionRoomCount = sessionRoomCounts.computeIfAbsent(
                roomId, key -> new AtomicInteger(0)
        );

        // 처음 붙는 거면 방 전체 접속자 수도 +1
        if (sessionRoomCount.incrementAndGet() == 1) {
            roomViewerCounts.computeIfAbsent(roomId, key -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public void markUnsubscribed(String sessionId, String subscriptionId) {
        if (isBlank(sessionId) || isBlank(subscriptionId)) {
            return;
        }

        ConcurrentMap<String, UUID> sessionSubscriptions = subscriptionsBySession.get(sessionId);
        if (sessionSubscriptions == null) {
            return;
        }

        UUID roomId = sessionSubscriptions.remove(subscriptionId);
        if (roomId == null) {
            return;
        }

        ConcurrentMap<UUID, AtomicInteger> sessionRoomCounts = roomCountsBySession.get(sessionId);
        if (sessionRoomCounts == null) {
            return;
        }

        AtomicInteger sessionRoomCount = sessionRoomCounts.get(roomId);
        if (sessionRoomCount == null) {
            return;
        }

        // 이 세션에서 마지막 구독이 빠지면 전체 접속자 수도 -1
        if (sessionRoomCount.decrementAndGet() <= 0) {
            sessionRoomCounts.remove(roomId);
            decrementGlobal(roomId);
        }

        cleanupSessionState(sessionId, sessionSubscriptions, sessionRoomCounts);
    }

    public void clearSession(String sessionId) {
        if (isBlank(sessionId)) {
            return;
        }

        // 연결이 끊기면 이 세션 값 전부 정리
        ConcurrentMap<String, UUID> sessionSubscriptions = subscriptionsBySession.remove(sessionId);
        ConcurrentMap<UUID, AtomicInteger> sessionRoomCounts = roomCountsBySession.remove(sessionId);

        if (sessionRoomCounts == null) {
            return;
        }

        sessionRoomCounts.forEach((roomId, count) -> {
            if (count != null && count.get() > 0) {
                decrementGlobal(roomId);
            }
        });

        if (sessionSubscriptions != null) {
            sessionSubscriptions.clear();
        }
    }

    public int getViewerCount(UUID roomId) {
        if (roomId == null) {
            return 0;
        }
        AtomicInteger count = roomViewerCounts.get(roomId);
        return count == null ? 0 : Math.max(count.get(), 0);
    }

    public Map<UUID, Integer> snapshotViewerCounts() {
        // 지금 숫자들을 한번 복사해서 가져감
        Map<UUID, Integer> snapshot = new HashMap<>();
        roomViewerCounts.forEach((roomId, count) -> {
            if (count != null && count.get() > 0) {
                snapshot.put(roomId, count.get());
            }
        });
        return Map.copyOf(snapshot);
    }

    private UUID extractRoomId(String destination) {
        if (isBlank(destination)) {
            return null;
        }

        // destination 이 /topic/rooms/{roomId}/messages 형식인지 확인
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

    private void decrementGlobal(UUID roomId) {
        // 전체 접속자 수에서 1 줄이고, 0이면 삭제
        roomViewerCounts.computeIfPresent(roomId, (key, counter) -> counter.decrementAndGet() > 0 ? counter : null);
    }

    private void cleanupSessionState(
            String sessionId,
            ConcurrentMap<String, UUID> sessionSubscriptions,
            ConcurrentMap<UUID, AtomicInteger> sessionRoomCounts
    ) {
        // 세션에 남은 값이 없으면 맵도 비움
        if (sessionSubscriptions.isEmpty()) {
            subscriptionsBySession.remove(sessionId);
        }
        if (sessionRoomCounts.isEmpty()) {
            roomCountsBySession.remove(sessionId);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
