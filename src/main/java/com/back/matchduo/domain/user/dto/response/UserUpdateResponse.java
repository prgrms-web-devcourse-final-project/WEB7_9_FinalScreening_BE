package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateResponse(
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        String password,

        @Schema(description = "닉네임", example = "nick")
        String nickname,

        @Schema(description = "자기소개", example = "자기 소개입니다")
        String comment
) {
    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getComment()
        );
    }
}
