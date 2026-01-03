package com.back.matchduo.integration;

import com.back.matchduo.domain.auth.dto.request.LoginRequest;
import com.back.matchduo.domain.auth.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // when
        ResponseEntity<LoginResponse> response = post("/api/v1/auth/login", request, LoginResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotNull();
        assertThat(response.getBody().user()).isNotNull();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_WrongPassword_Fail() {
        // given
        LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");

        // when
        ResponseEntity<String> response = post("/api/v1/auth/login", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_EmailNotFound_Fail() {
        // given
        LoginRequest request = new LoginRequest("notexist@test.com", TEST_PASSWORD);

        // when
        ResponseEntity<String> response = post("/api/v1/auth/login", request, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // when
        ResponseEntity<Void> response = postWithAuth("/api/v1/auth/logout", null, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}