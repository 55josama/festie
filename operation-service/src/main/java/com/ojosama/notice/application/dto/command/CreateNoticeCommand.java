package com.ojosama.notice.application.dto.command;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.UUID;

public record CreateNoticeCommand (
        UUID noticeId,
        String title,
        String content
){
    public Notice toEntity(){
        return Notice.of(
                noticeId, title, content
        );
    }
}
