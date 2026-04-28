package com.ojosama.report.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportTargetType {
    POST("게시글"),
    COMMENT("댓글"),
    CHAT("채팅 메시지");

    private final String description;
}
