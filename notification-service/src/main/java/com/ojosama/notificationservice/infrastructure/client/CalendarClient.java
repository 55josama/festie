package com.ojosama.notificationservice.infrastructure.client;

import com.ojosama.notificationservice.infrastructure.client.dto.CalendarUserInfo;
import com.ojosama.notificationservice.infrastructure.client.fallback.CalendarClientFallBackFactory;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "calendar-service", fallbackFactory = CalendarClientFallBackFactory.class)
public interface CalendarClient {

    @GetMapping("/internal/v1/calendars/{eventId}")
    CalendarUserInfo getCalendarUserInfo(@PathVariable("eventId") UUID eventId);
}
