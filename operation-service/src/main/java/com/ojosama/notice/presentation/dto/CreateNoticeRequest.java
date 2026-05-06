package com.ojosama.notice.presentation.dto;

import com.ojosama.notice.application.dto.command.CreateNoticeCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateNoticeRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
    public CreateNoticeCommand toCommand(UUID adminId) {
        return new CreateNoticeCommand(adminId, title, content);
    }
}
