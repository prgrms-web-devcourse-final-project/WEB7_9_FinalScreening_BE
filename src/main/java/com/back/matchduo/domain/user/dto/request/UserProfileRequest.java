package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileRequest(
        @Schema(description = "이미지", example = "imageURL")
        String profile_image
) {
}
