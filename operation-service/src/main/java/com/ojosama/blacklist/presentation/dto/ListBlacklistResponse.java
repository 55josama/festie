package com.ojosama.blacklist.presentation.dto;

import com.ojosama.blacklist.application.dto.result.BlacklistResult;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListBlacklistResponse (
        UUID id,
        UUID userId,
        BlacklistStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static ListBlacklistResponse from(BlacklistResult blacklistResult) {
        return new ListBlacklistResponse(
                blacklistResult.id(),
                blacklistResult.userId(),
                blacklistResult.status(),
                blacklistResult.reason(),
                blacklistResult.createdAt(),
                blacklistResult.updatedAt()
        );
    }
}
