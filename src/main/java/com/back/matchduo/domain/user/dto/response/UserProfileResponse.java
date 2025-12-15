package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileResponse(
        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @Schema(description = "이미지", example = "용량은 최대 10MB로 제한됩니다.")
        String profile_image,

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
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getEmail(),
                user.getProfile_image(),
                user.getNickname(),
                user.getComment()
        );
    }
}
