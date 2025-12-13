package com.back.matchduo.domain.user.dto.response;

import com.back.matchduo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileResponse(
        @Schema(description = "이미지", example = "imageURL")
        String profile_image
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getProfile_image()
        );
    }
}
