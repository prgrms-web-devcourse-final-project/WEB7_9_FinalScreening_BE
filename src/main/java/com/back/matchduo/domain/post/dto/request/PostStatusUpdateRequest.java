package com.back.matchduo.domain.post.dto.request;

import com.back.matchduo.domain.post.entity.PostStatus;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(
        @NotNull(message = "변경할 상태를 선택해주세요.")
        PostStatus status
) {
}
