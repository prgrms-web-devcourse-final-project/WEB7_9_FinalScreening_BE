package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateNicknameRequest(
        @Schema(description = "변경할 닉네임", defaultValue = "string", example = "string")
        String nickname
) {
}