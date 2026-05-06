package com.ojosama.notice.application.dto.result;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.UUID;

public record NoticeResult (
        UUID noticeId,
        UUID adminId,
        String title,
        String content
){
    public static NoticeResult from(Notice notice){
        return new NoticeResult(
                notice.getId(),
                notice.getAdminId(),
                notice.getTitle(),
                notice.getContent()
        );
    }
}
