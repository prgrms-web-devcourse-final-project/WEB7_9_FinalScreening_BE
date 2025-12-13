package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileImageUploadResponse(
        @Schema(description = "이미지")
        String profile_image
) {
    public static ProfileImageUploadResponse from(User user) {
        return new ProfileImageUploadResponse(
                user.getProfile_image()
        );
    }
}
