package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.Review;
import com.back.matchduo.domain.review.enums.ReviewEmoji;

import java.time.LocalDateTime;

public record ReviewUpdateResponse(
        Long reviewId,
        ReviewEmoji emoji,
        String content,
        LocalDateTime updatedAt
) {
    public static ReviewUpdateResponse from(Review review) {
        return new ReviewUpdateResponse(
                review.getId(),
                review.getEmoji(),
                review.getContent(),
                review.getUpdatedAt()
        );
    }
}
