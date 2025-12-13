package com.back.matchduo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileImageUploadRequest(
        @Schema(description = "이미지")
        String profile_image
) {
}
