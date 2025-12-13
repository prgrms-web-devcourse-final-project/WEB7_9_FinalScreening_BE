package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserSignUpResponse(
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @Schema(description = "비밀번호", example = "password123")
        String password,

        @Schema(description = "인증번호", example = "asd123")
        String verification_code
) {
    public static UserSignUpResponse from(User user) {
        return new UserSignUpResponse(
                user.getEmail(),
                user.getPassword(),
                user.getVerification_code()
        );
    }
}
