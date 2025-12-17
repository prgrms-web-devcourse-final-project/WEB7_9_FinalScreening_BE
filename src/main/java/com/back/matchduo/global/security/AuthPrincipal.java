package com.back.matchduo.global.security;

import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthPrincipal {

    private AuthPrincipal() {
    }

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_USER);
        }

        return (Long) authentication.getPrincipal();
    }
}

// 로그인한 유저 가져오는 유틸
/* import org.springframework.security.core.Authentication;

@PostMapping
public void createPost() {
Long userId = AuthPrincipal.getUserId(); 로 사용하면 됩니다.
*/
