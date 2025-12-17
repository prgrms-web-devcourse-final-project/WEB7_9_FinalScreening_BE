package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.user.entity.User;

public record PostWriter(
        Long userId,
        String nickname,
        String profileImageUrl
) {
    public static PostWriter from(User user) {
        return new PostWriter(
                user.getId(),
                user.getNickname(),
                user.getProfile_image()
        );
    }
}
