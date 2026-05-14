package com.ojosama.calendarservice.calendar.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojosama.calendarservice.calendar.domain.event.payload.EventImminentMessage;
import com.ojosama.calendarservice.calendar.domain.event.payload.TicketingImminentMessage;
import com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.producer.KafkaCalendarSchedulerProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpiredListener implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaCalendarSchedulerProducer publisher;

    // redisService 저장한 접두사와 일치 (비교를 위한)
    private static final String DATA_PREFIX = "alarm:data:";
    private static final String TRIGGER_PREFIX = "alarm:trigger:";

    // 락
    private static final String LOCK_KEY = "alarm:lock:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("만료 된 트리거 키 : {}", expiredKey);

        if (expiredKey.startsWith(TRIGGER_PREFIX)) {
            // 트리거 키에서 "TICKETING:uuid" 혹은 "EVENT:uuid" 형태의 식별자 추출
            String identifier = expiredKey.replace(TRIGGER_PREFIX, "");

            // 분산 락 시도
            String lockKey = LOCK_KEY + identifier;
            // 10초짜리 임시 키 생성
            Boolean isFirst = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "processing", java.time.Duration.ofSeconds(10));
            
            if (Boolean.TRUE.equals(isFirst)) {
                log.info("락 획독 성공 : {}", identifier);
                // 2. 아직 살아있는 쉐도우 키(Data Key)에서 JSON 꺼내기
                String dataKey = DATA_PREFIX + identifier;
                String json = redisTemplate.opsForValue().get(dataKey);

                if (json == null) {
                    log.warn("만료 신호는 왔으나 데이터를 찾을 수 없습니다: {}", dataKey);
                    return;
                }

                handleAlarm(identifier, json, dataKey);
            } else {
                log.info("이미 다른 서버에서 처리중인 알림입니다. {}", identifier);
            }

        }
    }

    private void handleAlarm(String identifier, String json, String dataKey) {
        try {
            // Redis에서 꺼낸 JSON을 바로 객체로 변환
            AlarmPayload payload = objectMapper.readValue(json, AlarmPayload.class);

            if (identifier.startsWith("TICKETING")) {
                publisher.publishTicketingEventImminent(new TicketingImminentMessage(
                        payload.eventId(),
                        payload.eventName(),
                        payload.alarmAt(), // 티켓팅 시간
                        payload.userIds()
                ));
            } else if (identifier.startsWith("EVENT")) {
                publisher.publishEventImminent(new EventImminentMessage(
                        payload.eventId(),
                        payload.eventName(),
                        payload.alarmAt(), // 행사 시간
                        payload.userIds()
                ));
            }

            // 발송 완료 후 쉐도우 데이터 삭제 (수동 청소)
            redisTemplate.delete(dataKey);
            log.info("알림 발송 완료 및 데이터 삭제: {}", identifier);

        } catch (JsonProcessingException e) {
            log.error("알림 데이터 복원 실패: {}", identifier, e);
        }
    }
}