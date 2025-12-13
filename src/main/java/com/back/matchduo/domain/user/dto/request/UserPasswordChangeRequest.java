package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserPasswordChangeRequest(
        @Schema(description = "비밀번호")
        String password
) {
}
