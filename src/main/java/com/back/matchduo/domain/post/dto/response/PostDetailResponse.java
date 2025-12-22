package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Post;

public record PostDetailResponse(
        Long postId,
        boolean isOwner
) {
    public static PostDetailResponse of(Post post) {
        return new PostDetailResponse(
                post.getId(),
                true
        );
    }
}