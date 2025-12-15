package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserLoginResponse(
        @NotBlank
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @NotBlank
        @Schema(description = "비밀번호", example = "password123")
        String password
) {
    public static  UserLoginResponse from(User user) {
        return new UserLoginResponse(
                user.getEmail(),
                user.getPassword()
        );
    }
}
