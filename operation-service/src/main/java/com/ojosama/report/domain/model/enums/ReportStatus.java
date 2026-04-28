package com.ojosama.report.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("대기 중/자동 블라인드"), // 신고 접수됨 또는 AI에 의해 임시 숨김
    RESOLVED("제재 확정"),          // 관리자 확인 후 악성 글로 판별됨 (제재 유지)
    REJECTED("오인 신고/반려");       // 관리자 확인 후 정상 글로 판별됨 (제재 해제)

    private final String description;
}
