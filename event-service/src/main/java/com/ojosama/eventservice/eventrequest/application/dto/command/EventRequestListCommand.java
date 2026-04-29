package com.ojosama.eventservice.eventrequest.application.dto.command;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record EventRequestListCommand(
        EventRequestStatus status,
        String categoryName,
        String eventName,
        UUID requesterId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdStart,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdEnd
) {}
