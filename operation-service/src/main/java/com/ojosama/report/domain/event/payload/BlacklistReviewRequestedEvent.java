package com.ojosama.report.domain.event.payload;

import java.util.UUID;

public record BlacklistReviewRequestedEvent(
        UUID targetUserId,
        String reason,
        long blindCount
) { }
