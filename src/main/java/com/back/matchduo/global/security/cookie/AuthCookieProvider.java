package com.back.matchduo.global.security.cookie;

import com.back.matchduo.global.config.CookieProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {

    // 쿠키 이름 (Auth 파트에서만 사용)
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final CookieProperties cookieProperties;

    public AuthCookieProvider(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    // Access Token 쿠키 생성
    public ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds) {
        return buildCookie(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds);
    }

    // Refresh Token 쿠키 생성
    public ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return buildCookie(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds);
    }

    // Access Token 쿠키 만료
    public ResponseCookie expireAccessTokenCookie() {
        return buildCookie(ACCESS_TOKEN_COOKIE, "", 0);
    }

    // Refresh Token 쿠키 만료
    public ResponseCookie expireRefreshTokenCookie() {
        return buildCookie(REFRESH_TOKEN_COOKIE, "", 0);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
