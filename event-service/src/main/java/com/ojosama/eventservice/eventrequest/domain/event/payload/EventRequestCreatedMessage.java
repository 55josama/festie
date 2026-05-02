package com.ojosama.eventservice.eventrequest.domain.event.payload;

import java.util.UUID;

public record EventRequestCreatedMessage(
        UUID targetId,
        String categoryName,
        String eventName
) {}
