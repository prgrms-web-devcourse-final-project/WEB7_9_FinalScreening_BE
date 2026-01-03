package com.back.matchduo.integration;

import com.back.matchduo.domain.post.dto.response.PostListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PostIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("모집글 목록 조회 성공 - 인증 없이")
    void getPostList_WithoutAuth_Success() {
        // when
        ResponseEntity<PostListResponse> response = get("/api/v1/posts", PostListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("모집글 목록 조회 성공 - 인증 포함")
    void getPostList_WithAuth_Success() {
        // when
        ResponseEntity<PostListResponse> response = getWithAuth("/api/v1/posts", PostListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("모집글 목록 조회 - 상태 필터링")
    void getPostList_WithStatusFilter_Success() {
        // when
        ResponseEntity<PostListResponse> response = get("/api/v1/posts?status=RECRUIT", PostListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("모집글 목록 조회 - 큐타입 필터링")
    void getPostList_WithQueueTypeFilter_Success() {
        // when
        ResponseEntity<PostListResponse> response = get("/api/v1/posts?queueType=SOLO_RANK", PostListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("모집글 목록 조회 - 게임모드 필터링")
    void getPostList_WithGameModeFilter_Success() {
        // when
        ResponseEntity<PostListResponse> response = get("/api/v1/posts?gameMode=SUMMONERS_RIFT", PostListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("모집글 삭제 실패 - 인증 없음")
    void deletePost_WithoutAuth_Fail() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/posts/999",
                org.springframework.http.HttpMethod.DELETE,
                null,
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("모집글 삭제 실패 - 존재하지 않는 모집글")
    void deletePost_NotFound_Fail() {
        // when
        ResponseEntity<String> response = deleteWithAuth("/api/v1/posts/999999", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}