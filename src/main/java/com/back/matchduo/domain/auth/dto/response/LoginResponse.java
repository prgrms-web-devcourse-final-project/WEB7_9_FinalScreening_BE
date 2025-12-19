package com.back.matchduo.domain.auth.dto.response;

public record LoginResponse(
        AuthUserSummary user,
        String accessToken,
        String refreshToken
) {
}
