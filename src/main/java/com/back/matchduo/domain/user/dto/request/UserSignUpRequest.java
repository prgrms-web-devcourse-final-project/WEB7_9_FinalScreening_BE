package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequest(
        @NotBlank
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @NotBlank
        @Schema(description = "비밀번호", example = "password123!")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-z\\d!@#$%^&*]{8,100}$",
                message = "비밀번호는 8글자 이상, 영어 소문자와 숫자, 특수문자(!, @, #, $, %, ^, &, *)를 포함해야 합니다."
        )
        String password,

        @NotBlank
        @Schema(description = "비밀번호 확인", example = "password123!")
        String passwordConfirm,

        @NotBlank
        @Schema(description = "인증번호", example = "asd123")
        String verificationCode
) { }
