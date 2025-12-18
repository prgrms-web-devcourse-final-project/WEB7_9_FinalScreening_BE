package com.back.matchduo.domain.review.dto.request;

import com.back.matchduo.domain.review.enums.ReviewEmoji;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
        @NotNull(message = "평가는 필수입니다.")
        ReviewEmoji emoji,

        @Size(max = 100, message = "리뷰 내용은 100자 이내여야 합니다.")
        String content
) {
}
