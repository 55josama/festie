package com.ojosama.notificationservice.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.notificationservice.infrastructure.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener {

    private final SseEmitterManager sseEmitterManager;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 넘어온 메시지(JSON)를 알림 DTO로 변환
            NotificationDto notification = objectMapper.readValue(message.getBody(), NotificationDto.class);

            // 내 서버에 이 유저가 연결되어 있다면 sse 전송
            sseEmitterManager.sendToUser(notification.userId(), notification.data());
        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 처리 에러: {}", e.getMessage());
        }
    }
}