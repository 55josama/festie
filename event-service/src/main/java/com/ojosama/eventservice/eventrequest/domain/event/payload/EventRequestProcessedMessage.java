package com.ojosama.eventservice.eventrequest.domain.event.payload;

import java.util.UUID;

public record EventRequestProcessedMessage(
        UUID targetId,
        UUID receiverId,
        String resultStatus,
        String eventName
) {}
