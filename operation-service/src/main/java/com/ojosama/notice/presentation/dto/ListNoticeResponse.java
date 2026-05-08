package com.ojosama.notice.presentation.dto;

import com.ojosama.notice.application.dto.result.NoticeResult;
import java.util.UUID;

public record ListNoticeResponse(
        UUID noticeId,
        UUID adminId,
        String title,
        String content
) {
    public static ListNoticeResponse from(NoticeResult result){
        return new ListNoticeResponse(
                result.noticeId(),
                result.adminId(),
                result.title(),
                result.content()
        );
    }
}
