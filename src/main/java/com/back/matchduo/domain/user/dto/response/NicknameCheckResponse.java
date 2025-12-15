package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameCheckResponse(
        @Schema(description = "닉네임", example = "nick")
        String nickname
) {
    public static NicknameCheckResponse from(User user) {
        return new NicknameCheckResponse (
                user.getNickname()
        );
    }
}
