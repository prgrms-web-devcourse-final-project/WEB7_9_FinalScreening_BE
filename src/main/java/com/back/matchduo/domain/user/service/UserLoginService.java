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
    private final AuthCookieProvider cookieProvider;

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