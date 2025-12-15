package com.back.matchduo.domain.post.dto.response;

public record PostDeleteResponse(
        Long postId,
        Boolean deleted
) {
    public static PostDeleteResponse of(Long postId) {
        return new PostDeleteResponse(postId, true);
    }
}
