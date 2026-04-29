package com.ojosama.favoriteservice.infrastructure.client;

import com.ojosama.favoriteservice.infrastructure.client.dto.EventInfoResponseDto;
import com.ojosama.favoriteservice.infrastructure.client.fallback.EventClientFallBackFactory;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", fallbackFactory = EventClientFallBackFactory.class)
public interface EventClient {

    @GetMapping("/internal/v1/events/{eventId}")
    EventInfoResponseDto getEvents(@PathVariable UUID eventId);
}
