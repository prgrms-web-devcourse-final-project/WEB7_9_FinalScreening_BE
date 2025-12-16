package com.back.matchduo.domain.review.dto.request;

import com.back.matchduo.domain.review.enums.ReviewEmoji;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull(message = "모집글 ID는 필수입니다.")
        Long postId,

        @NotNull(message = "리뷰 대상자 ID는 필수입니다.")
        Long revieweeId,

        @NotNull(message = "평가는 필수입니다.")
        ReviewEmoji reviewEmoji,

        @Size(max = 100, message = "리뷰 내용은 100자 이내여야 합니다.")
        String content
) {}
