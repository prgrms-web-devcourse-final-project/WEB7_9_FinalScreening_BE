package com.back.matchduo.global.security.filter;

import com.back.matchduo.global.security.cookie.AuthCookieProvider;
import com.back.matchduo.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // accessToken 쿠키에서 JWT 추출
        String accessToken = extractCookie(request, AuthCookieProvider.ACCESS_TOKEN_COOKIE);

        if (accessToken != null && !accessToken.isBlank()) {
            try {
                // 토큰 유효성 검증
                jwtProvider.validate(accessToken);

                // 토큰에서 userId 추출
                Long userId = jwtProvider.getUserId(accessToken);

                // Role 사용 안 함 → authorities 비워둠
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of()
                        );

                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 토큰이 잘못된 경우 인증 정보 제거
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        return
                // 회원가입
                (uri.equals("/api/v1/users/signup") && method.equals("POST")) ||

                        // 로그인
                        (uri.equals("/api/v1/auth/login") && method.equals("POST")) ||

                        // 이메일 인증 관련
                        uri.startsWith("/api/v1/users/email") ||

                        // 토큰 재발급
                        (uri.equals("/api/v1/auth/refresh") && method.equals("POST"));
    }
}
