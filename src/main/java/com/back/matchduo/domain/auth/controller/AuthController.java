package com.back.matchduo.domain.auth.controller;

import com.back.matchduo.domain.auth.dto.request.LoginRequest;
import com.back.matchduo.domain.auth.dto.response.LoginResponse;
import com.back.matchduo.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증/인가", description = "로그인, 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. 성공 시 accessToken과 refreshToken이 쿠키로 자동 설정됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "user": {
                                        "userId": 1,
                                        "email": "test@example.com",
                                        "nickname": "테스트유저"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "이메일이 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "비밀번호가 일치하지 않음")
    })
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpServletResponse res) {
        return authService.login(req, res);
    }

    @Operation(
            summary = "토큰 갱신",
            description = "RefreshToken으로 새로운 AccessToken을 발급받습니다. RefreshToken은 쿠키에서 자동으로 추출됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "user": {
                                        "userId": 1,
                                        "email": "test@example.com",
                                        "nickname": "테스트유저"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "RefreshToken이 유효하지 않음")
    })
    @PostMapping("/refresh")
    public LoginResponse refresh(HttpServletRequest req, HttpServletResponse res) {
        return authService.refresh(req, res);
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃 처리합니다. DB의 RefreshToken이 삭제되고 쿠키가 만료됩니다. 응답 Body는 비어있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공 (응답 Body 없음)"
            )
    })
    @PostMapping("/logout")
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(req, res);
    }
}
