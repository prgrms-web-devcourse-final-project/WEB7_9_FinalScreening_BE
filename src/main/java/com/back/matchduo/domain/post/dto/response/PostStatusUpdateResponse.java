package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;

import java.time.LocalDateTime;

public record PostStatusUpdateResponse(
        Long postId,
        PostStatus status,
        LocalDateTime updatedAt
) {
    public static PostStatusUpdateResponse of(Post post) {
        return new PostStatusUpdateResponse(
                post.getId(),
                post.getStatus(),
                post.getUpdatedAt()
        );
    }
}
