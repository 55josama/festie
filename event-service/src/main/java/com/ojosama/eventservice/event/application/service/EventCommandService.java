package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import java.util.UUID;

public interface EventCommandService {
    EventResult createEvent(CreateEventCommand command);
    EventResult updateEvent(UpdateEventCommand command);
    void deleteEvent(UUID userId, UUID eventId);
}
