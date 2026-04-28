package com.ojosama.comment.domain.model;

public enum CommentStatus {
    UNVERIFIED, // 검증 전
    CLEAN,      // 정상
    REPORTED,   // 신고됨
    BLOCKED     // 차단됨
}
