package com.ojosama.eventservice.event.application.service;

import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventResult;

public interface EventCommandService {
    EventResult createEvent(CreateEventCommand command);
}
