package com.ojosama.operationrequest.presentation.dto;

import com.ojosama.operationrequest.application.dto.command.UpdateOperationRequestCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateOperationRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
    public UpdateOperationRequestCommand toCommand(UUID requesterId) {
        return new UpdateOperationRequestCommand(requesterId, title, content);
    }
}
