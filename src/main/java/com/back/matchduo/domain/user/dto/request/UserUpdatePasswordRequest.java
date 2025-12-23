package com.back.matchduo.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdatePasswordRequest(
        @NotBlank
        String password,

        @NotBlank
        String newPassword,

        @NotBlank
        String newPasswordConfirm
) {
}