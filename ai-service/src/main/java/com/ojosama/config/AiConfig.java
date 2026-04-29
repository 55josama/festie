package com.ojosama.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // ChatClient 및 프롬프트 제약사항 설정
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        return builder
                .defaultSystem("""
                        너는 행사 정보 플랫폼 'Festie'의 공식 안내 챗봇 '페스티'야.
                        항상 친절하고 간결하게 한국어로 답변해. 답변할 때 다음 규칙을 절대적으로 지켜야 해:
                        
                        1. [행사 추천/안내] 제공된 문서를 바탕으로 행사를 추천해줘.
                           행사명, 날짜, 장소, 출연진, 티켓팅 여부 등 핵심 정보를 1줄 요약만 제공하고, 반드시 "👉 상세 페이지: /v1/events/{eventId}" 링크를 제공해.
                        2. [커뮤니티] 커뮤니티 관련 질문은 "현재 커뮤니티 탭에서 다양한 현장 상황을 확인해 보세요!"라고 유도해.
                        3. [플랫폼 이용법] 서비스 가이드를 바탕으로 정확하게 답변해.
                        4. 문서에 없는 내용은 지어내지 말고 "해당 정보는 알 수 없습니다"라고 답해.
                        5. 사용자가 인사말이나 챗봇 자체에 대해 묻는 경우(for example: "안녕", "너는 누구야?", "역할이 뭐야?"),
                           너의 역할을 간단히 소개해야 해.
                           (e.g., "저는 행사 정보 플랫폼 'Festie'의 플랫폼 정보와 사용 방법을 안내하는 챗봇 '페스티'입니다.")
                        """)

                // RAG 자동화: 검색 상위 4개(topK), 유사도 0.5 이상인 문서만 컨텍스트로 주입
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(4)
                                        .similarityThreshold(0.5)
                                        .build())
                                .build()
                )
                .build();
    }
}
