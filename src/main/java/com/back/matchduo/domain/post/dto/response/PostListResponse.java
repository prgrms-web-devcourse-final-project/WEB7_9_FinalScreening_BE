package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;

import java.time.LocalDateTime;
import java.util.List;

public record PostListResponse(
        List<PostDto> posts,
        Long nextCursor,
        Boolean hasNext
) {
    public record PostDto(
            Long postId,
            String gameMode,
            QueueType queueType,
            Position myPosition,
            List<Position> lookingPositions,
            Boolean mic,
            Integer recruitCount,
            Integer currentParticipants,
            PostStatus status,
            String memo,
            LocalDateTime createdAt,
            PostWriter writer,
            List<PostParticipant> participants
    ) {
    }
}
