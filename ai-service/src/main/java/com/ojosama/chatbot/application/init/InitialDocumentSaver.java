package com.ojosama.chatbot.application.init;

import com.ojosama.chatbot.application.service.DocumentIndexer;
import com.ojosama.chatbot.infrastructure.client.EventClient;
import com.ojosama.chatbot.infrastructure.client.dto.EventClientResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDocumentSaver {
    private final DocumentIndexer documentIndexer;
    private final EventClient eventClient;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialData() {
        log.info("Chatbot Service: 초기 지식 데이터 동기화 시작...");

        try {
            loadSystemGuides();

            ApiResponse<List<EventClientResponse>> response = eventClient.getEvents();

            if (response != null && response.getData() != null) {
                List<EventClientResponse> events = response.getData();

                for (EventClientResponse event : events) {
                    documentIndexer.indexEvent(
                            event.id(),
                            event.name(),
                            event.categoryName(),
                            event.startAt() != null ? event.startAt().toString() : "미정",
                            event.endAt() != null ? event.endAt().toString() : "미정",
                            event.place(),
                            event.hasTicketing(),
                            event.officialLink(),
                            event.description(),
                            event.performer(),
                            event.status()
                    );
                }
                log.info("Chatbot Service: 행사 데이터 {}건 동기화 완료", events.size());
            }
        } catch (Exception e) {
            log.error("Chatbot Service: 데이터 초기 동기화 중 오류 발생", e);
        }
    }

    private void loadSystemGuides() {
        // 플랫폼 개요 및 기본 사용법
        documentIndexer.indexGuide("intro_festie", "Festie는 축제, 콘서트, 팬미팅, 팝업스토어 정보를 통합 제공하고 사용자들이 실시간으로 소통하는 플랫폼입니다.");
        documentIndexer.indexGuide("event_search", "행사 목록 조회 시 카테고리 필터(축제, 콘서트, 팬미팅, 팝업스토어)와 검색 기능을 통해 원하는 정보를 쉽게 찾을 수 있습니다.");
        documentIndexer.indexGuide("event_request_guide", "찾으시는 행사가 없다면 '행사 추가 요청'을 통해 제보할 수 있습니다. 관리자 승인 후 플랫폼에 정식 등록됩니다.");

        // 찜하기 및 캘린더 기능
        documentIndexer.indexGuide("favorite_calendar", "행사 상세 페이지에서 '찜하기'를 누르면 내 개인 캘린더에 해당 일정이 자동으로 등록됩니다.");
        documentIndexer.indexGuide("monthly_calendar", "전체 행사 일정을 한눈에 보고 싶다면 월간 캘린더 메뉴를 이용해 보세요. 카테고리별 필터링도 가능합니다.");

        // 실시간 채팅방 운영 정책
        documentIndexer.indexGuide("chat_open_policy", "각 행사별 공식 채팅방은 행사 시작 전 지정된 시간에 자동으로 개설되며, 사용자가 직접 생성할 수는 없습니다.");
        documentIndexer.indexGuide("chat_close_policy", "채팅방은 행사 종료 후 지정된 시간이 지나면 자동으로 종료 및 폐쇄됩니다.");
        documentIndexer.indexGuide("chat_auth_distinction", "채팅방에서는 위치 인증을 완료한 유저와 미인증 유저의 메시지가 구분되어 표시될 수 있습니다.");

        // 커뮤니티 가이드라인
        documentIndexer.indexGuide("community_categories", "커뮤니티는 꿀팁, 자유, 후기, 요청 카테고리로 운영됩니다. 각 행사에 특화된 정보를 공유해 보세요.");
        documentIndexer.indexGuide("post_interaction", "게시글에 댓글과 답글을 달 수 있으며, 유익한 글에는 '좋아요'를 눌러 응원할 수 있습니다.");

        // 신고 및 제재 시스템
        documentIndexer.indexGuide("forbidden_word_policy", "Festie는 쾌적한 환경을 위해 Redis 기반 금지어 필터링을 적용합니다. 부적절한 단어가 포함된 글은 저장 및 노출이 제한됩니다.");
        documentIndexer.indexGuide("auto_blind_policy", "유저 신고가 3회 누적되거나 AI가 명백한 악성 콘텐츠로 판단할 경우, 해당 게시글이나 채팅은 즉시 자동 블라인드(숨김) 처리됩니다.");
        documentIndexer.indexGuide("manager_review", "블라인드된 콘텐츠는 카테고리 매니저의 사후 검토를 거쳐 제재 유지(RESOLVED) 또는 복구(REJECTED) 처리가 결정됩니다.");
        documentIndexer.indexGuide("blacklist_criteria", "유효한 제재(블라인드 유지) 횟수가 총 5회 누적된 사용자는 관리자 검토 후 블랙리스트에 등록되어 서비스 이용이 영구 제한될 수 있습니다.");

        // 알림 서비스 이용 안내
        documentIndexer.indexGuide("noti_types", "찜한 행사의 티켓팅 임박 알림, 행사 D-7, D-1, 당일 알림을 제공하며 내 글의 댓글/좋아요 알림도 실시간으로 발송됩니다.");
        documentIndexer.indexGuide("noti_box", "모든 알림은 웹 내부 알림함에서 확인 가능하며, 중요한 소식은 실시간 토스트 알림으로 알려드립니다.");

        // AI 챗봇 이용 및 한계
        documentIndexer.indexGuide("chatbot_function", "챗봇 '페스티'는 사이트 이용 방법 안내, 행사 정보 요약, 비슷한 행사 추천 기능을 제공합니다.");
        documentIndexer.indexGuide("chatbot_routing", "챗봇이 제공하는 정보는 요약본이므로, 정확한 출연진 정보나 티켓팅 링크는 반드시 행사 상세 페이지에서 확인해야 합니다.");
    }
}
