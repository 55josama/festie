package com.ojosama.common.kafka.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

//표준을 맞추는 용도로 여기에 이벤트 타입 (private String eventType) 적어주시면 됩니다
//해당 서비스만 쓰는 내부 이벤트 이름은 작성 x
@Getter
@RequiredArgsConstructor
public enum EventType {

    // event
    EVENT_CREATED("EventCreated"),
    EVENT_DELETED("EventDeleted"),
    EVENT_UPDATED("EventUpdated"),
    EVENT_SCHEDULE_CHANGED("EventScheduleChanged"),

    EVENT_REQUEST_CREATED("EventRequestCreated"),
    EVENT_REQUEST_RESULT("EventRequestResultCreated"),

    // calendar
    TICKETING_SCHEDULE_REMINDER("EventTicketingSchedule"),
    SCHEDULE_REMINDER("EventSchedule"),
    CALENDAR_UPDATED("CalendarUpdated"),
    CALENDAR_DELETED("CalendarDeleted"),

    // operation
    REPORT_BLINDED("ReportBlinded"),
    REPORT_UNBLINDED("ReportUnblinded"),
    BLACKLIST_REGISTERED("BlacklistRegistered"),
    BLACKLIST_UPDATED("BlacklistUpdated"),
    BLACKLIST_REVIEW_REQUESTED("BlacklistReviewRequested"),
    OPERATION_REQUEST_CREATED("OperationRequestCreated"),
    TARGET_BLINDED("TargetBlinded"),
    USER_BLACKLIST_STATUS_UPDATED("UserBlacklistStatusUpdated"),

    //community
    POST_DELETED("PostDeleted"),
    TARGET_UNBLINDED("TargetUnblinded"),

    // ai
    AI_MODERATION_EVALUATED("AiModerationEvaluated"),
    CHAT_MODERATION_REQUESTED("ChatModerationRequested"),
    COMMUNITY_MODERATION_REQUESTED("CommunityModerationRequested");

    private final String value;

    // 문자열 값을 가지고 Enum 객체를 찾아주는 편의 메서드
    public static EventType fromValue(String value) {
        return Arrays.stream(EventType.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown EventType: " + value));
    }
}
