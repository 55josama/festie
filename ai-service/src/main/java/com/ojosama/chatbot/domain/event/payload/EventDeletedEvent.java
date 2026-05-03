package com.ojosama.chatbot.domain.event.payload;

import java.util.UUID;

public record EventDeletedEvent(
        UUID eventId,
        String eventName
) { }
