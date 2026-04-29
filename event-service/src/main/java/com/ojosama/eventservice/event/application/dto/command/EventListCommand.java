package com.ojosama.eventservice.event.application.dto.command;

import com.ojosama.eventservice.event.domain.model.EventStatus;
import java.time.LocalDateTime;

public record EventListCommand(
    String category,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer year,
    Integer month
) {}
