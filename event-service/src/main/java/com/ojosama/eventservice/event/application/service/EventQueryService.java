package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.util.UUID;

public interface EventQueryService {
    EventResult getEventById(UUID id);
}
