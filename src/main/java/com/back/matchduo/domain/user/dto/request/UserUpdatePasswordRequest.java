package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdatePasswordRequest(
        // //수정한 부분
        @Schema(description = "현재 비밀번호", defaultValue = "string", example = "string")
        String password,

        @Schema(description = "새 비밀번호", defaultValue = "string", example = "string")
        String newPassword,

        @Schema(description = "새 비밀번호 확인", defaultValue = "string", example = "string")
        String newPasswordConfirm
        // //수정한 부분
) {
}