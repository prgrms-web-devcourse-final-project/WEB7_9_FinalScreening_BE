package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.Review;
import com.back.matchduo.domain.review.enums.ReviewEmoji;

import java.time.LocalDateTime;

public record MyReviewListResponse(
    Long reviewId,
    Long revieweeId,
    String revieweeNickname,
    ReviewEmoji emoji,
    String content,
    LocalDateTime createdAt
) {
    public static MyReviewListResponse from(Review review) {
        return new MyReviewListResponse(
            review.getId(),
            review.getReviewee().getId(),
            review.getReviewee().getNickname(),
            review.getEmoji(),
            review.getContent(),
            review.getCreatedAt()
        );
    }
}