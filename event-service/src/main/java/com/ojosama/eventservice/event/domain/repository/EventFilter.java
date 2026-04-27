package com.ojosama.eventservice.event.domain.repository;

import com.ojosama.eventservice.event.domain.model.EventStatus;
import java.time.LocalDateTime;

public record EventFilter(
    String categoryName,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer year,
    Integer month
) {}
