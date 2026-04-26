package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import java.util.UUID;

public record FindBlacklistResponse (UUID id, UUID userid, String status, String reason){
    public static FindBlacklistResponse from(BlacklistResult result) {
        return new FindBlacklistResponse(
                result.id(),
                result.userId(),
                result.status(),
                result.reason()
        );
    }
}
