package com.back.matchduo.domain.review.controller;

import com.back.matchduo.domain.review.dto.request.ReviewCreateRequest;
import com.back.matchduo.domain.review.dto.request.ReviewUpdateRequest;
import com.back.matchduo.domain.review.dto.response.*;
import com.back.matchduo.domain.review.service.ReviewRequestService;
import com.back.matchduo.domain.review.service.ReviewService;
import com.back.matchduo.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRequestService reviewRequestService;

    @Operation(summary = "리뷰 작성", description = "같이 게임한 유저의 리뷰를 작성합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 작성 성공")
    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest req
    ) {
        Long currentUserId = null;
        if (userDetails != null) currentUserId = userDetails.getId();

        ReviewCreateResponse response = reviewService.createReview(currentUserId,req);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 수정", description = "작성했던 파티원의 리뷰를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 수정 성공")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewUpdateResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest req
    ) {
        Long currentUserId = null;
        if (userDetails != null) currentUserId = userDetails.getId();

        ReviewUpdateResponse response = reviewService.updateReview(reviewId,currentUserId,req);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "리뷰 삭제", description = "작성했던 리뷰를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        Long currentUserId = null;
        if (userDetails != null) currentUserId = userDetails.getId();

        reviewService.deleteReview(reviewId,currentUserId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "리뷰작성가능한 리뷰요청관리 목록 조회", description = "작성가능한 상태의 리뷰요청관리목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰요청관리 목록 조회 성공")
    @GetMapping("/requests")
    public ResponseEntity<List<ReviewRequestResponse>> getWritableReviewRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = null;
        if (userDetails != null) currentUserId = userDetails.getId();

        List<ReviewRequestResponse> responses = reviewRequestService.getWritableReviewRequests(currentUserId);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "리뷰목록 조회", description = "특정 유저가 받은 리뷰목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰목록 조회 성공")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewListResponse>> getReviewsReceivedByUser(
            @PathVariable Long userId
    ) {
        List<ReviewListResponse> responses = reviewService.getReviewsReceivedByUser(userId);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "내 리뷰작성목록 조회", description = "내가 작성했던 리뷰목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰작성목록 조회 성공")
    @GetMapping("/me")
    public ResponseEntity<List<MyReviewListResponse>> getMyWrittenReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = null;
        if (userDetails != null) currentUserId = userDetails.getId();

        List<MyReviewListResponse> responses = reviewService.getMyWrittenReviews(currentUserId);

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "특정 유저가 받은 리뷰비율 조회", description = "유저가 받은 리뷰 이모지의 비율분포를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰비율 조회 성공")
    @GetMapping("/users/{userId}/distribution")
    public ResponseEntity<ReviewDistributionResponse> getReviewDistribution(
            @PathVariable Long userId
    ) {
        ReviewDistributionResponse response = reviewService.getReviewDistribution(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "모든 리뷰 조회", description = "모든 유저가 받은 리뷰 통합 조회")
    @ApiResponse(responseCode = "200", description = "리뷰 통합 조회 성공")
    @GetMapping
    public ResponseEntity<List<ReviewListResponse>> getAllReviews(
    ) {
        List<ReviewListResponse> responses = reviewService.getAllReviews();

        return ResponseEntity.ok(responses);
    }
}
