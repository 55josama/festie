package com.ojosama.operationrequest.application.dto.command;

import com.ojosama.operationrequest.domain.model.entity.OperationRequest;
import java.util.UUID;

public record CreateOperationRequestCommand(
        UUID requesterId,
        String title,
        String content
) {
    public OperationRequest toEntity(){
        return OperationRequest.of(
                requesterId, title, content
        );
    }
}
