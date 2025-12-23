package com.back.matchduo.domain.user.service;

import com.back.matchduo.domain.auth.refresh.repository.RefreshTokenRepository;
import com.back.matchduo.domain.user.dto.request.UserLoginRequest;
import com.back.matchduo.domain.user.dto.response.UserLoginResponse;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import com.back.matchduo.global.security.cookie.AuthCookieProvider;
import com.back.matchduo.global.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
public class UserLoginService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final AuthCookieProvider cookieProvider;

    //로그인 기능
    public UserLoginResponse login(UserLoginRequest request) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new CustomException(CustomErrorCode.NOT_FOUND_USER)
                );

        //비밀번호
        if (!user.getPassword().equals(request.password())) {
            throw new CustomException(CustomErrorCode.WRONG_PASSWORD);
        }

        return new UserLoginResponse(
                user.getId(),
                user.getEmail()
        );
    }

    //로그아웃 기능
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        // 1. 쿠키에서 리프레시 토큰 추출
        String refreshToken = extractCookie(req, AuthCookieProvider.REFRESH_TOKEN_COOKIE);

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                // 2. 토큰 검증 및 유저 식별
                jwtProvider.validate(refreshToken);
                Long userId = jwtProvider.getUserId(refreshToken);

                // 3. DB에서 해당 유저의 리프레시 토큰 삭제
                refreshTokenRepository.deleteByUserId(userId);
            } catch (Exception e) {
                // 검증 실패 시에도 로그아웃(쿠키 삭제)은 계속 진행
            }
        }

        // 4. 응답 헤더에 쿠키 만료 설정 추가 (브라우저 쿠키 삭제)
        res.addHeader("Set-Cookie", cookieProvider.expireAccessTokenCookie().toString());
        res.addHeader("Set-Cookie", cookieProvider.expireRefreshTokenCookie().toString());
    }

    // [헬퍼 메서드: 쿠키 추출]
    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    //회원 탈퇴 기능
    public void resign(Long userId, HttpServletResponse res) {
        // 1. DB에서 해당 유저의 리프레시 토큰 삭제 (로그아웃 로직과 동일)
        refreshTokenRepository.deleteByUserId(userId);

        // 2. DB에서 유저 엔티티 삭제
        userRepository.deleteById(userId);

        // 3. 클라이언트의 쿠키 만료 처리
        res.addHeader("Set-Cookie", cookieProvider.expireAccessTokenCookie().toString());
        res.addHeader("Set-Cookie", cookieProvider.expireRefreshTokenCookie().toString());
    }
}