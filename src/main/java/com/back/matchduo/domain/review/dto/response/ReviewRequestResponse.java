package com.back.matchduo.domain.review.dto.response;

import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;

import java.time.LocalDateTime;

public record ReviewRequestResponse(
        Long reviewRequestId,
        Long postId,
        String postMemo,
        ReviewRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static ReviewRequestResponse from(ReviewRequest request) {
        return new ReviewRequestResponse(
            request.getId(),
            request.getPost().getId(),
            request.getPost().getMemo(),
            request.getStatus(),
            request.getCreatedAt(),
            request.getExpiresAt()
        );
    }
}