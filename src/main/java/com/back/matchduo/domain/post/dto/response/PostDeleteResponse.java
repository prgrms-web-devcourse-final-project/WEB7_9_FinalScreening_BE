package com.back.matchduo.domain.post.dto.response;

public record PostDeleteResponse(
        Long postId,
        Boolean deleted,
        String message
) {
    public static PostDeleteResponse of(Long postId) {
        return new PostDeleteResponse(
                postId,
                true,
                postId + "번 모집글이 정상적으로 삭제되었습니다."
        );
    }
}
