package com.ojosama.chatservice.application.scheduler;

import com.ojosama.chatservice.application.service.ChatRoomService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomScheduler {

    private static final String LOCK_KEY = "chat:scheduler:chat-room-status";
    private static final long LOCK_TTL_SECONDS = 90L;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) "
                    + "else return 0 end",
            Long.class
    );

    private final ChatRoomService chatRoomService;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 60000) // 1 min
    public void syncChatRoomStatus() {
        String lockToken = acquireLock();
        if (lockToken == null) { // 락 성공 토큰을 받지 못하면 null
            log.info("채팅방 스케쥴러가 이미 다른 인스턴스에서 실행 중입니다.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int openedCount = 0;
        int closedCount = 0;

        try {
            try { // try-finally 추가, 락 건거 해제하는걸 finally
                openedCount = chatRoomService.openScheduledChatRooms(now);
            } catch (Exception e) {
                log.error("스케쥴러가 채팅방 오픈에 실패했습니다. now={}", now, e);
            }

            try {
                closedCount = chatRoomService.closeScheduledChatRooms(now);
            } catch (Exception e) {
                log.error("스케쥴러가 채팅방 종료에 실패했습니다. now={}", now, e);
            }

            log.info("채팅방 스케쥴러가 실행되었습니다. openedCount={}, closedCount={}, now={}",
                    openedCount, closedCount, now);
        } finally { // 락 해제 (락토큰)
            releaseLock(lockToken);
        }
    }

    private String acquireLock() {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, token, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            return null;
        }
        return token;
    }

    private void releaseLock(String token) {
        if (token == null) {
            return;
        }
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY), token);
    }
}
