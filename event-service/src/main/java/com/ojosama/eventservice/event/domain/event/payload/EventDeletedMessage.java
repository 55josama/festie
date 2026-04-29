package com.ojosama.eventservice.event.domain.event.payload;

import java.util.UUID;

public record EventDeletedMessage(
        UUID eventId,
        String eventName
) {}
