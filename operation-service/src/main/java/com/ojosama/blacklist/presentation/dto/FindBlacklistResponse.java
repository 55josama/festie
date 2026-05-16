package com.ojosama.blacklist.presentation.dto;

import com.ojosama.blacklist.application.dto.result.BlacklistResult;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record FindBlacklistResponse (
        UUID id,
        UUID userId,
        BlacklistStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static FindBlacklistResponse from(BlacklistResult result) {
        return new FindBlacklistResponse(
                result.id(),
                result.userId(),
                result.status(),
                result.reason(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
