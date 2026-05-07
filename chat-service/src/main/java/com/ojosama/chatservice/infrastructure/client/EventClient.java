package com.ojosama.chatservice.infrastructure.client;

import com.ojosama.chatservice.infrastructure.client.dto.InternalEventLocationResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/internal/v1/events/{eventId}/location")
    InternalEventLocationResponse getInternalEventLocation(@PathVariable("eventId") UUID eventId);
}
