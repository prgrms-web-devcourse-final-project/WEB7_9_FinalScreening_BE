package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.Review;
import com.back.matchduo.domain.review.enums.ReviewEmoji;

import java.time.LocalDateTime;

public record ReviewListResponse(
        Long reviewId,
        Long reviewerId,
        String reviewerNickname,
        ReviewEmoji emoji,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewListResponse from(Review review) {
        return new ReviewListResponse(
            review.getId(),
            review.getReviewer().getId(),
            review.getReviewer().getNickname(),
            review.getEmoji(),
            review.getContent(),
            review.getCreatedAt()
        );
    }
}