package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.Review;
import com.back.matchduo.domain.review.enums.ReviewEmoji;

import java.time.LocalDateTime;

public record ReviewListResponse(
        Long reviewId,
        Long reviewerId,
        Long revieweeId,
        String reviewerNickname,
        String revieweeNickname,
        String reviewerProfileImage,
        ReviewEmoji emoji,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewListResponse from(Review review) {
        return new ReviewListResponse(
            review.getId(),
            review.getReviewer().getId(),
            review.getReviewee().getId(),
            review.getReviewer().getNickname(),
            review.getReviewee().getNickname(),
            review.getReviewer().getProfileImage(),
            review.getEmoji(),
            review.getContent(),
            review.getCreatedAt()
        );
    }
}