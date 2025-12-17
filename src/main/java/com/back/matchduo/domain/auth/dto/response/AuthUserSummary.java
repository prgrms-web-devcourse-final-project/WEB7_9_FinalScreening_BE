package com.back.matchduo.domain.auth.dto.response;

public record AuthUserSummary(
        Long userId,
        String email,
        String nickname
) {
}
// API LoginResponse 응답 전용으로 사용 : userId, email, nickname 요약 DTO