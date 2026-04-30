package com.ojosama.operationrequest.application.dto.result;

import com.ojosama.operationrequest.domain.model.entity.OperationRequest;
import com.ojosama.operationrequest.domain.model.enums.OperationRequestStatus;
import java.util.UUID;

public record OperationRequestResult (
        UUID id,
        UUID requesterId,
        String title,
        String content,
        OperationRequestStatus status,
        String adminMemo
){
    public static OperationRequestResult from(OperationRequest operationRequest){
        return new OperationRequestResult(
                operationRequest.getId(),
                operationRequest.getRequesterId(),
                operationRequest.getTitle(),
                operationRequest.getContent(),
                operationRequest.getStatus(),
                operationRequest.getAdminMemo()
        );
    }
}
