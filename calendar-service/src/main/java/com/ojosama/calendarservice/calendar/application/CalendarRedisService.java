package com.ojosama.calendarservice.calendar.application;

import java.time.Duration;
import java.time.LocalDateTime;
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

    private static final String TICKETING_ALARM_KEY = "ticketing-alarm:";
    private static final String EVENT_ALARM_KEY = "event-alarm:";

    public void registerTicketingAlarm(UUID eventId, LocalDateTime ticketingDate) {
        // TTL = 티켓팅 시간 - 1시간 - 현재 시간
        long ttl = Duration.between(LocalDateTime.now(), ticketingDate.minusHours(1)).getSeconds();
        // TTL이 0 이하(이미 지난 경우)면 등록 x
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    TICKETING_ALARM_KEY + eventId,
                    eventId.toString(),
                    ttl,
                    TimeUnit.SECONDS
            );
            log.info("티켓팅 알림 redis 등록 : {}", eventId);
        }
    }

    public void registerEventAlarm(UUID eventId, LocalDateTime eventDate) {
        // TTL = 행사 전날 13:00 - 현재 시간
        LocalDateTime alarmTime = eventDate.toLocalDate().atTime(13, 0);
        // TTL이 0 이하(이미 지난 경우)면 등록 x
        long ttl = Duration.between(LocalDateTime.now(), alarmTime).getSeconds();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    EVENT_ALARM_KEY + eventId,
                    eventId.toString(),
                    ttl,
                    TimeUnit.SECONDS
            );
            log.info("행사 알림 redis 등록 : {}", eventId);
        }
    }

    public void deleteAlarms(UUID eventId) {
        redisTemplate.delete(TICKETING_ALARM_KEY + eventId);
        redisTemplate.delete(EVENT_ALARM_KEY + eventId);
        log.info("알림 삭제 : {}", eventId);
    }
}