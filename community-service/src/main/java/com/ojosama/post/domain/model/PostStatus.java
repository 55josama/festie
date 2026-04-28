package com.ojosama.post.domain.model;

public enum PostStatus {
    UNVERIFIED, // 검증 전
    CLEAN,      // 정상
    REPORTED,   // 신고됨
    BLOCKED     // 차단됨
}
