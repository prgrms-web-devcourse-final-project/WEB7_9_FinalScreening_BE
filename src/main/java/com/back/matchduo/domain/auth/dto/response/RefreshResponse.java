package com.back.matchduo.domain.auth.dto.response;

public record RefreshResponse(
        AuthUserSummary user,
        String accessToken
) {
}
