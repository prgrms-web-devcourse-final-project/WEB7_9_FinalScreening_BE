package com.back.matchduo.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateCommentRequest(
        @Size(max = 40, message = "자기소개는 최대 40글자까지 작성할 수 있습니다.")
        String comment
) {
}