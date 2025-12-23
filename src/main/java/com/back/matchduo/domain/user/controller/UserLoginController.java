package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserLoginRequest;
import com.back.matchduo.domain.user.dto.response.UserLoginResponse;
import com.back.matchduo.domain.user.service.UserLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserLoginController {
    private final UserLoginService userLoginService;

    //로그인 기능
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody
            @Valid
            UserLoginRequest request
    ) {
        UserLoginResponse response = userLoginService.login(request);
        return ResponseEntity.ok(response);
    }

    //로그아웃 기능
    @PostMapping("/logout")
    public ResponseEntity<UserLoginResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        userLoginService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    //회원 탈퇴 기능
    @PostMapping("/resign")
    public ResponseEntity<UserLoginResponse> resign(
            @RequestAttribute("userId")
            Long userId, // [참고] 인증 필터에서 설정한 유저 PK

            HttpServletResponse response
    ){
        userLoginService.resign(userId, response);
        return ResponseEntity.noContent().build();
    }
}