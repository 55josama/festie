package com.ojosama.notificationservice.infrastructure.sse;

import com.ojosama.notificationservice.infrastructure.redis.NotificationDto;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitterManager {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;

    //  레디스로 메시지를 발행
    public void broadcast(UUID userId, Object data) {
        redisTemplate.convertAndSend("notification-topic", new NotificationDto(userId, data));
    }

    // 실제 브라우저로 SSE 이벤트를 전송
    public void sendToUser(UUID userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } catch (Exception e) {
                emitters.remove(userId, emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
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
            try {
                oldEmitter.complete();
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        // 연결 종료/타임아웃/에러 시 Map에서 깔끔하게 제거
        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> emitters.remove(userId, emitter));
        emitter.onError(t -> emitters.remove(userId, emitter));

        try {
            // 최초 연결 확인 이벤트 전송
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (Exception e) {
            emitters.remove(userId, emitter);
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        return emitter;
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        if (!emitters.isEmpty()) {
            log.debug("전역 SSE Heartbeat 핑 전송 시작. 현재 연결 수: {}", emitters.size());
        }

        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (Exception e) {
                log.debug("하트비트 실패로 커넥션 정리 (유저 ID: {})", userId);
                emitters.remove(userId, emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        });
    }
}