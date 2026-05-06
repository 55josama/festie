package com.ojosama.report.domain.event;

import com.ojosama.report.domain.event.payload.BlacklistReviewRequestedEvent;
import com.ojosama.report.domain.event.payload.TargetBlindedEvent;

public interface ReportEventProducer {
    // 타겟(게시글/채팅)이 3회 누적되어 자동 블라인드 되었음을 알리는 이벤트
    void publishTargetBlindEvent(TargetBlindedEvent event);

    // 유저의 블라인드 횟수가 5회 누적되어 블랙리스트 등록 대상임을 알리는 이벤트
    void publishBlacklistReviewRequestEvent(BlacklistReviewRequestedEvent event);
}
