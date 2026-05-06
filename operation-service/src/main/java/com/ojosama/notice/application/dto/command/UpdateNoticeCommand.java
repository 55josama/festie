package com.ojosama.notice.application.dto.command;

import java.util.UUID;

public record UpdateNoticeCommand(
        UUID noticeId,
        String title,
        String content
) { }
