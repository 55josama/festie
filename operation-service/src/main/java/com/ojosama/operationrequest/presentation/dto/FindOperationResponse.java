package com.ojosama.operationrequest.presentation.dto;

import com.ojosama.operationrequest.application.dto.result.OperationRequestResult;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import java.util.UUID;

public record FindOperationResponse(
        UUID id,
        UUID requesterId,
        String title,
        String content,
        OperationRequestStatus status,
        String adminMemo
) {
    public static FindOperationResponse from(OperationRequestResult result) {
        return new FindOperationResponse(
                result.id(),
                result.requesterId(),
                result.title(),
                result.content(),
                result.status(),
                result.adminMemo()
        );
    }
}
