package com.back.matchduo.domain.user.controller;

import com.back.matchduo.domain.user.dto.request.UserLoginRequest;
import com.back.matchduo.domain.user.dto.response.UserLoginResponse;
import com.back.matchduo.domain.user.service.UserLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserLoginController {
    private final UserLoginService userLoginService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @RequestBody
            @Valid
                UserLoginRequest request
    ) {
        UserLoginResponse response = userLoginService.login(request);
        return ResponseEntity.ok(response);
    }
}

