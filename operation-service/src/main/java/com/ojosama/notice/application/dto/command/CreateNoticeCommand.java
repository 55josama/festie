package com.ojosama.notice.application.dto.command;

import com.ojosama.notice.domain.model.entity.Notice;
import java.util.UUID;

public record CreateNoticeCommand (
        UUID adminId,
        String title,
        String content
){
    public Notice toEntity(){
        return Notice.of(
                adminId, title, content
        );
    }
}
