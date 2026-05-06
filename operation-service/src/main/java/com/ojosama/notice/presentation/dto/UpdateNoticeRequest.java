package com.ojosama.notice.presentation.dto;

import com.ojosama.notice.application.dto.command.UpdateNoticeCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateNoticeRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
    public UpdateNoticeCommand toCommand(UUID noticeId){
        return new UpdateNoticeCommand(noticeId, title, content);
    }
}
