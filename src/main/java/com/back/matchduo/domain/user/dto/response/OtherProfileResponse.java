package com.back.matchduo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record OtherProfileResponse(
        @Schema(description = "유저 고유 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "커뮤니티닉네임")
        String nickname,

        @Schema(description = "프로필 이미지 경로", example = "/upload/profile/uuid_image.png")
        String profile_image,

        @Schema(description = "자기소개", example = "인간시대의 끝이 도래했다")
        String comment,

        @Schema(description = "게임 계정 고유 ID", example = "123")
        Long gameAccountId
) {

}
