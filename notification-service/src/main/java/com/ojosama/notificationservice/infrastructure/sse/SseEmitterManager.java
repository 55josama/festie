package com.ojosama.notificationservice.infrastructure.sse;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseEmitterManager {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void sendToUser(UUID userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        log.info("현재 연결된 유저 수: {}", emitters.size());

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException e) {
                log.error("SseEmitter send error: {}, userId: {}", e.getMessage(), userId);
                emitters.remove(userId);
            }
        } else {
            log.error("SseEmitter not found: {}", userId);
        }
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        // 연결 끊기면 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(t -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

}
