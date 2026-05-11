package com.ojosama.notice.application.dto.command;

import java.util.UUID;

public record UpdateNoticeCommand(
        UUID adminId,
        String title,
        String content
) { }
