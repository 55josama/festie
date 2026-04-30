package com.ojosama.eventservice.eventrequest.domain.repository;

import com.ojosama.eventservice.eventrequest.domain.model.EventRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventRequestFilter(
        EventRequestStatus status,
        String categoryName,
        String eventName,
        UUID requesterId,
        LocalDateTime createdStart,
        LocalDateTime createdEnd
) {}
