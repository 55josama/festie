package com.ojosama.comment.domain.event.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;

//게시글 소프트 삭제 이벤트
//community.post.deleted.v1
//deletedBy는 게시글을 삭제한 사용자 UUID이며 로깅용, 댓글의 deletedBy는 deletedBy = null로 처리
@JsonIgnoreProperties(ignoreUnknown = true)
public record PostDeletedEvent(
        UUID postId,
        UUID deletedBy,
        LocalDateTime deletedAt
) {
}
