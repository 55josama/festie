package com.ojosama.post.domain.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

//게시글 생성 시 발행 — AI 서비스가 부적절 검증을 위해 수신. Domain Event
//Post가 만들어지면 무조건 이 스티커(Event)가 생긴다는 규칙을 틀(도메인)안에 넣어놓기 위해 domain계층에 존재.
//게시글이 생성됨이라는 비즈니스 언어의 일부
// Post.create()가 호출되는 시점에 어떤 정보가 핵심인지 정의하는 것은 도메인의 권한. 따라서 PostCreatedEvent는 도메인 객체의 상태 변경을 가장 잘 대변하는 도메인의 파생물
//계층형 아키텍처의 의존성 방향 application -> domain(의존없음)
public record PostCreatedEvent(
        UUID postId,
        UUID userId,
        UUID categoryId,
        String title,
        String content,
        LocalDateTime occurredAt
) {
}

