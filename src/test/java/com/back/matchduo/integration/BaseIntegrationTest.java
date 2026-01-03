package com.back.matchduo.integration;

import com.back.matchduo.domain.auth.dto.request.LoginRequest;
import com.back.matchduo.domain.auth.dto.response.LoginResponse;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GameAccountRepository gameAccountRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    // 테스트용 유저 정보
    protected static final String TEST_EMAIL = "test@test.com";
    protected static final String TEST_PASSWORD = "password123!";
    protected static final String TEST_NICKNAME = "testUser";

    protected String accessToken;
    protected Long testUserId;
    protected User testUser;

    @BeforeEach
    void setUpBase() {
        // 기존 테스트 데이터 정리
        cleanUp();

        // 테스트 유저 생성
        testUser = createTestUser(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
        testUserId = testUser.getId();

        // 로그인하여 토큰 획득
        accessToken = loginAndGetToken(TEST_EMAIL, TEST_PASSWORD);
    }

    protected void cleanUp() {
        // 외래키 제약조건 순서대로 삭제: game_account -> user
        gameAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createTestUser(String email, String password, String nickname) {
        // AuthService가 평문 비교를 하므로 암호화 없이 저장
        User user = User.createUser(
                email,
                password,
                nickname
        );
        return userRepository.save(user);
    }

    protected String loginAndGetToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                LoginResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().accessToken();
        }

        throw new RuntimeException("로그인 실패: " + response.getStatusCode());
    }

    protected HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected <T> ResponseEntity<T> getWithAuth(String url, Class<T> responseType) {
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, request, responseType);
    }

    protected <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        return restTemplate.getForEntity(url, responseType);
    }

    protected <T> ResponseEntity<T> postWithAuth(String url, Object body, Class<T> responseType) {
        HttpEntity<Object> request = new HttpEntity<>(body, createAuthHeaders());
        return restTemplate.postForEntity(url, request, responseType);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, Class<T> responseType) {
        HttpEntity<Object> request = new HttpEntity<>(body, createHeaders());
        return restTemplate.postForEntity(url, request, responseType);
    }

    protected <T> ResponseEntity<T> deleteWithAuth(String url, Class<T> responseType) {
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.DELETE, request, responseType);
    }

    protected <T> ResponseEntity<T> patchWithAuth(String url, Object body, Class<T> responseType) {
        HttpEntity<Object> request = new HttpEntity<>(body, createAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.PATCH, request, responseType);
    }

    protected <T> ResponseEntity<T> putWithAuth(String url, Object body, Class<T> responseType) {
        HttpEntity<Object> request = new HttpEntity<>(body, createAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
    }

    protected String createUrl(String path) {
        return "http://localhost:" + port + path;
    }
}