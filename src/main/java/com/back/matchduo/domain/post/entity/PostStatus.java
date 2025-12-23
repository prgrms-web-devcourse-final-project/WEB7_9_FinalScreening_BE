package com.back.matchduo.domain.post.entity;

public enum PostStatus {
    RECRUIT, // 모집 중 (인원이 다 안 참)
    ACTIVE,  // 모집 완료 (인원이 다 참 -> 이때부터 6시간 타이머 시작)
    CLOSED   // 종료 (수동 종료 or 시간 초과)
}
