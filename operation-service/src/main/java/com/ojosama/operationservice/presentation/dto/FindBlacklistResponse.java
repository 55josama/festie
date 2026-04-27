package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.model.enums.RegistrationType;
import java.util.UUID;

public record FindBlacklistResponse (
        UUID id,
        UUID userid,
        BlacklistStatus status,
        String reason,
        RegistrationType registrationType
){
    public static FindBlacklistResponse from(BlacklistResult result) {
        return new FindBlacklistResponse(
                result.id(),
                result.userId(),
                result.status(),
                result.reason(),
                result.registrationType()
        );
    }
}
