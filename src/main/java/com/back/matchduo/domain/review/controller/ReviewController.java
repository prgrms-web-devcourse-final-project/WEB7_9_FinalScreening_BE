package com.back.matchduo.domain.review.controller;

import com.back.matchduo.domain.review.dto.request.ReviewCreateRequest;
import com.back.matchduo.domain.review.dto.request.ReviewUpdateRequest;
import com.back.matchduo.domain.review.dto.response.*;
import com.back.matchduo.domain.review.service.ReviewRequestService;
import com.back.matchduo.domain.review.service.ReviewService;
import com.back.matchduo.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRequestService reviewRequestService;

    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest req
    ) {
        Long currentUserId = null;
        if (userDetails != null) {
            currentUserId = userDetails.getId();
        }

        ReviewCreateResponse response = reviewService.createReview(currentUserId,req);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewUpdateResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId,
            @Valid @RequestBody ReviewUpdateRequest req
    ) {
        ReviewUpdateResponse response = reviewService.updateReview(reviewId,userId,req);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId,userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ReviewRequestResponse>> getWritableReviewRequests(
            @RequestParam Long userId
    ) {
        List<ReviewRequestResponse> responses = reviewRequestService.getWritableReviewRequests(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewListResponse>> getReviewsReceivedByUser(
            @PathVariable Long userId
    ) {
        List<ReviewListResponse> responses = reviewService.getReviewsReceivedByUser(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyReviewListResponse>> getMyWrittenReviews(
            @RequestParam Long userId
    ) {
        List<MyReviewListResponse> responses = reviewService.getMyWrittenReviews(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/{userId}/distribution")
    public ResponseEntity<ReviewDistributionResponse> getReviewDistribution(
            @PathVariable Long userId
    ) {
        ReviewDistributionResponse response = reviewService.getReviewDistribution(userId);

        return ResponseEntity.ok(response);
    }
}
