package com.back.matchduo.domain.auth.controller;

import com.back.matchduo.domain.auth.dto.request.LoginRequest;
import com.back.matchduo.domain.auth.dto.response.LoginResponse;
import com.back.matchduo.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpServletResponse res) {
        return authService.login(req, res);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(HttpServletRequest req, HttpServletResponse res) {
        return authService.refresh(req, res);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(req, res);
    }
}
