package com.ojosama.calendarservice.calendar.infrastructure.client;

import com.ojosama.calendarservice.calendar.infrastructure.client.dto.GetEventInfo;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/v1/events/{eventId}")
    GetEventInfo getEventInfo(@PathVariable("eventId") UUID eventId);
}
