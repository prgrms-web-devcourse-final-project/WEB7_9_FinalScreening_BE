package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewCreateResponse (
        Long reviewId,
        Long reviewerId,
        Long revieweeId,
        LocalDateTime createdAt
) {
    public static ReviewCreateResponse from(Review review) {
        return new ReviewCreateResponse(
                review.getId(),
                review.getReviewer().getId(),
                review.getReviewee().getId(),
                review.getCreatedAt()
        );
    }
}
