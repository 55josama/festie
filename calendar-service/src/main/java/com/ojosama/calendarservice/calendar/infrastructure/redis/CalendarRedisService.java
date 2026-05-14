package com.ojosama.calendarservice.calendar.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DATA_PREFIX = "alarm:data:";
    private static final String TRIGGER_PREFIX = "alarm:trigger:";

    // 티켓팅 알림 등록
    public void registerTicketingAlarm(UUID eventId, String eventName, LocalDateTime ticketingDate,
                                       List<UUID> userIds) {
        long ttl = Duration.between(LocalDateTime.now(), ticketingDate.minusHours(1)).getSeconds();
        if (ttl > 0) {
            AlarmPayload payload = new AlarmPayload(eventId, eventName, ticketingDate, userIds);
            saveAlarm("TICKETING", eventId, payload, ttl);
        }
    }

    // 행사 알림 등록
    public void registerEventAlarm(UUID eventId, String eventName, LocalDateTime eventDate, List<UUID> userIds) {
        LocalDateTime alarmTime = eventDate.toLocalDate().atTime(13, 0);
        long ttl = Duration.between(LocalDateTime.now(), alarmTime).getSeconds();
        if (ttl > 0) {
            AlarmPayload payload = new AlarmPayload(eventId, eventName, alarmTime, userIds);
            saveAlarm("EVENT", eventId, payload, ttl);
        }
    }

    // 공통 저장 로직 (Shadow Key 적용)
    private void saveAlarm(String type, UUID eventId, AlarmPayload payload, long ttl) {
        try {
            String identifier = type + ":" + eventId;
            String json = objectMapper.writeValueAsString(payload);

            // 데이터 저장 (이미 있으면 덮어쓰지 않음 - 멱등성)
            // 데이터 키는 트리거보다 조금 더 길게 유지
            redisTemplate.opsForValue().set(DATA_PREFIX + identifier, json, ttl + 600, TimeUnit.SECONDS);

            // 트리거 저장
            // 시간을 알리기 위한 것. 데이터는 x
            redisTemplate.opsForValue().setIfAbsent(TRIGGER_PREFIX + identifier, "", ttl, TimeUnit.SECONDS);

            log.info("{} 알림 등록 완료: {}", type, eventId);
        } catch (JsonProcessingException e) {
            log.error("{} 알림 등록 실패: {}", type, eventId, e);
        }
    }

    public void deleteAlarms(UUID eventId) {
        // 티켓팅, 행사 데이터와 트리거 모두 삭제 시도
        redisTemplate.delete(List.of(
                DATA_PREFIX + "TICKETING:" + eventId, TRIGGER_PREFIX + "TICKETING:" + eventId,
                DATA_PREFIX + "EVENT:" + eventId, TRIGGER_PREFIX + "EVENT:" + eventId
        ));
        log.info("알림 삭제 완료: {}", eventId);
    }
}