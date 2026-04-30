package com.ojosama.operationrequest.domain.event.payload;

import java.util.UUID;

public record OperationRequestCreatedEvent(
        UUID requestId,
        UUID requesterId,
        String title
) { }
