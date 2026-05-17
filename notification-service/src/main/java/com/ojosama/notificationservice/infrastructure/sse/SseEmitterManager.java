package com.ojosama.notificationservice.infrastructure.sse;

import com.ojosama.notificationservice.infrastructure.redis.NotificationDto;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitterManager {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskScheduler taskScheduler;

    public void broadcast(UUID userId, Object data) {
        redisTemplate.convertAndSend("notification-topic", new NotificationDto(userId, data));
    }

    public void sendToUser(UUID userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } catch (IOException e) {
                emitters.remove(userId, emitter);
            }
        } else {
            log.debug("접속 중이 아닌 유저: {}", userId);
        }
    }

    public SseEmitter subscribe(UUID userId) {
        // 30분 타임아웃 SSE 연결 생성
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // 기존 연결 있으면 닫기
        SseEmitter oldEmitter = emitters.put(userId, emitter);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        // 30초마다 heartbeat를 보내서 연결을 유지
        ScheduledFuture<?> heartbeat = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (IOException e) {
                // heartbeat 실패 = 연결 끊김 → map에서 제거
                emitters.remove(userId, emitter);
            }
        }, 30000);

        // 연결 끊기면 map에서 제거 + 스케줄러도 같이 취소
        emitter.onCompletion(() -> {
            emitters.remove(userId, emitter);
            heartbeat.cancel(true);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            heartbeat.cancel(true);
        });
        emitter.onError(t -> {
            emitters.remove(userId, emitter);
            heartbeat.cancel(true);
        });

        try {
            // 최초 연결 확인 이벤트 전송
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }

        return emitter;
    }
}
