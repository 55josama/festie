package com.ojosama.calendarservice.calendar.infrastructure.client.dto;

import java.time.LocalDateTime;

public record EventInfoResponseDto(
        String name,
        String status,
        LocalDateTime ticketingOpenAt
) {
}
