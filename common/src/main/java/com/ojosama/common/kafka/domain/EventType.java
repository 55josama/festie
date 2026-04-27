package com.ojosama.common.kafka.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

//표준을 맞추는 용도로 여기에 이벤트 타입 (private String eventType) 적어주시면 됩니다
//해당 서비스만 쓰는 내부 이벤트 이름은 작성 x
@Getter
@RequiredArgsConstructor
public enum EventType {

    // Outbound
    POST_CREATED("PostCreated"),
    POST_UPDATED("PostUpdated"),
    COMMENT_CREATED("CommentCreated"),
    COMMENT_UPDATED("CommentUpdated"),
    POST_REPORTED("PostReported"),
    COMMENT_REPORTED("CommentReported");

    private final String value;

    // 문자열 값을 가지고 Enum 객체를 찾아주는 편의 메서드
    public static EventType fromValue(String value) {
        return Arrays.stream(EventType.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown EventType: " + value));
    }
}
