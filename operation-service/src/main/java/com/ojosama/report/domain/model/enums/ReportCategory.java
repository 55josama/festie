package com.ojosama.report.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportCategory {
    PROFANITY("욕설 및 비방"),
    HATE_SPEECH("혐오 표현"),
    SEXUAL_CONTENT("음란성 및 성적 표현"),
    SPAM("도배 및 스팸"),
    SCAM("사기 및 허위 정보 의심"),
    PRIVACY_LEAK("개인정보 노출"),
    UNAUTHORIZED_TRADE("불법 거래 유도"),
    OTHER("기타 부적절한 내용");

    private final String description;
}