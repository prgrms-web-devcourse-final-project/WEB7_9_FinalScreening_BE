package com.back.matchduo.domain.user.dto.response;

public record TokenRefreshResponse(String accessToken, String refreshToken) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return new TokenRefreshResponse(accessToken, refreshToken);
    }
}
