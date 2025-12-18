package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @Schema(description = "이미지", example = "용량은 최대 10MB로 제한됩니다.")
        String profile_image,

        @Schema(description = "비밀번호", example = "password123")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-z\\d!@#$%^&*]{8,30}$",
                message = "비밀번호는 8글자 이상, 영어 소문자와 숫자, 특수문자를 포함해야 합니다."
        )
        String password,

        @Schema(description = "새 비밀번호", example = "password456")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-z\\d!@#$%^&*]{8,30}$",
                message = "비밀번호는 8글자 이상, 영어 소문자와 숫자, 특수문자를 포함해야 합니다."
        )
        String newPassword,

        @Schema(description = "새 비밀번호 확인", example = "password456")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-z\\d!@#$%^&*]{8,30}$",
                message = "비밀번호는 8글자 이상, 영어 소문자와 숫자, 특수문자를 포함해야 합니다."
        )
        String newPasswordConfirm,

        @NotBlank
        @Schema(description = "닉네임", example = "nick")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]{2,8}$",
                message = "닉네임은 2~8자의 한글, 영문, 숫자만 사용할 수 있으며 공백과 특수문자및 비속어는 사용할 수 없습니다."
        )
        String nickname,

        @Schema(description = "자기소개", example = "자기 소개입니다")
        @Size(max = 40, message = "자기소개는 최대 40글자까지 작성할 수 있습니다.")
        String comment
) {
}
