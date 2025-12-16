package com.back.matchduo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserLoginResponse(
        long userId,

        @NotBlank
        @Schema(description = "이메일", example = "user@email.com")
        String email
) {
}
