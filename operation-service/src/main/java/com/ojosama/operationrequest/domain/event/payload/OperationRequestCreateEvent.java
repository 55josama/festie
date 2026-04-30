package com.ojosama.operationrequest.domain.event.payload;

import java.util.UUID;

public record OperationRequestCreateEvent(
        UUID requestId,
        UUID requesterId,
        String title
) { }
