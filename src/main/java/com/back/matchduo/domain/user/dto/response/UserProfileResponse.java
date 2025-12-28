package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserProfileResponse(
        @Schema(description = "유저 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@email.com")
        String email,

        @Schema(description = "이미지", example = "프로필 이미지 URL")
        String profileImage,

        @NotBlank
        @Schema(description = "닉네임", example = "nick")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9]{2,8}$",
                message = "닉네임은 2~8자의 한글, 영문, 숫자만 사용할 수 있습니다."
        )
        String nickname,

        @Schema(description = "자기소개", example = "자기 소개입니다")
        @Size(max = 40, message = "자기소개는 최대 40글자까지 작성할 수 있습니다.")
        String comment,

        @Schema(description = "닉네임 최근 수정 시각", example = "2025-01-01T12:00:00")
        LocalDateTime nicknameUpdatedAt
) {

    public static UserProfileResponse from(User user) {
        String profileImage = user.getProfileImage();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                profileImage,
                user.getNickname(),
                user.getComment(),
                user.getNicknameUpdatedAt()
        );
    }
}