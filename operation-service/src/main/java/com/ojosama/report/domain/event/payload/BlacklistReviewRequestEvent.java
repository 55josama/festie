package com.ojosama.report.domain.event.payload;

import java.util.UUID;

public record BlacklistReviewRequestEvent(
        UUID targetUserId,
        String reason,
        long blindCount
) { }
