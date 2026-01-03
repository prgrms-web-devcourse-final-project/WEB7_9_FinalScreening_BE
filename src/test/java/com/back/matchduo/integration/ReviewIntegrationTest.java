package com.back.matchduo.integration;

import com.back.matchduo.domain.review.dto.response.ReviewDistributionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("모든 리뷰 조회 성공 - 인증 없이")
    void getAllReviews_WithoutAuth_Success() {
        // when
        ResponseEntity<String> response = get("/api/v1/reviews", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("특정 유저 리뷰 조회 성공")
    void getReviewsByUser_Success() {
        // when
        ResponseEntity<String> response = get("/api/v1/reviews/users/" + testUserId, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("특정 유저 리뷰 분포 조회 성공")
    void getReviewDistribution_Success() {
        // when
        ResponseEntity<ReviewDistributionResponse> response = get(
                "/api/v1/reviews/users/" + testUserId + "/distribution",
                ReviewDistributionResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("내 리뷰 작성 목록 조회 성공 - 인증 필요")
    void getMyWrittenReviews_WithAuth_Success() {
        // when
        ResponseEntity<String> response = getWithAuth("/api/v1/reviews/me", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("내 리뷰 작성 목록 조회 실패 - 인증 없음")
    void getMyWrittenReviews_WithoutAuth_Fail() {
        // when
        ResponseEntity<String> response = get("/api/v1/reviews/me", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("리뷰 요청 목록 조회 성공 - 인증 필요")
    void getWritableReviewRequests_WithAuth_Success() {
        // when
        ResponseEntity<String> response = getWithAuth("/api/v1/reviews/requests", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("리뷰 요청 목록 조회 실패 - 인증 없음")
    void getWritableReviewRequests_WithoutAuth_Fail() {
        // when
        ResponseEntity<String> response = get("/api/v1/reviews/requests", String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰 또는 권한 없음")
    void deleteReview_NotFound_Fail() {
        // when
        ResponseEntity<String> response = deleteWithAuth("/api/v1/reviews/999999", String.class);

        // then - 존재하지 않는 리뷰는 FORBIDDEN 또는 NOT_FOUND 반환 가능
        assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
    }
}