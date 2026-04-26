package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import java.util.UUID;

public record ListBlacklistResponse (
        UUID id,
        UUID userId,
        String status
){
    public static ListBlacklistResponse from(BlacklistResult result) {
        return new ListBlacklistResponse(
                result.id(),
                result.userId(),
                result.status()
        );
    }
}
