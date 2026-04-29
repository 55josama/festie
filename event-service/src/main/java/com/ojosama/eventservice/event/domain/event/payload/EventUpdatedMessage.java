package com.ojosama.eventservice.event.domain.event.payload;

import java.util.UUID;

public record EventUpdatedMessage(
        UUID eventId,
        String eventName
) {}
