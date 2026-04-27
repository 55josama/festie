package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.EventListCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventQueryService {
    Page<EventResult> getEvents(EventListCommand command, Pageable pageable);
    EventResult getEventById(UUID id);
}
