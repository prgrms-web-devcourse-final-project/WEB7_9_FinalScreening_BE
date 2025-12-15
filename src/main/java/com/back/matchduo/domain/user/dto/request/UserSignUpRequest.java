package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequest(
        @NotBlank
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @NotBlank
        @Schema(description = "비밀번호", example = "password123")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*])[a-z\\d!@#$%^&*]{8,100}$",
                message = "비밀번호는 8글자 이상, 영어 소문자와 숫자, 특수문자(!, @, #, $, %, ^, &, *)를 포함해야 합니다."
        )
        String password,

        //비속어는 Service에서 제한 예정
        @NotBlank
        @Schema(description = "닉네임", example = "nick")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]{2,8}$",
                message = "닉네임은 2~8자의 한글, 영문, 숫자만 사용할 수 있으며 공백과 특수문자및 비속어는 사용할 수 없습니다."
        )
        String nickname,

        @NotBlank
        @Schema(description = "인증번호", example = "asd123")
        String verification_code
) { }
