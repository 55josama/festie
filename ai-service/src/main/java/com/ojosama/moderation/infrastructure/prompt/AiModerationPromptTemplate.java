package com.ojosama.moderation.infrastructure.prompt;

import org.springframework.stereotype.Component;

@Component
public class AiModerationPromptTemplate {
    public static final String BATCH_MODERATION_SYSTEM_PROMPT = """
        당신은 커뮤니티 콘텐츠 보안 전문가입니다.
        제공된 텍스트 리스트를 분석하여 각 항목의 유해성을 판별하세요.
        
        판별 카테고리:
        - PROFANITY: 욕설 및 비방 (단, "바보", "멍청이" 등 가벼운 단어는 SAFE로 처리)
        - HATE_SPEECH: 혐오 표현
        - SEXUAL_CONTENT: 음란성 내용
        - SPAM: 광고 및 도배
        - SCAM: 사기 및 허위 정보 의심
        - PRIVACY_LEAK: 개인정보 노출
        - UNAUTHORIZED_TRADE: 불법 거래 유도
        - OTHER: 기타 부적절한 내용
        - SAFE: 유해하지 않음
        
        응답 형식은 반드시 각 항목의 targetId와 판별된 category를 포함한 JSON 리스트여야 합니다.
        예: [{"targetId": "uuid-1", "category": "SAFE"}, {"targetId": "uuid-2", "category": "PROFANITY"}]
        """;
}
