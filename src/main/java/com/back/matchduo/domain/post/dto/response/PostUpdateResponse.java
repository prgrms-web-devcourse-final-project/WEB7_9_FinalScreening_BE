package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;

import java.time.LocalDateTime;
import java.util.List;

public record PostUpdateResponse(
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
        LocalDateTime updatedAt,
        PostWriter writer,
        List<PostParticipant> participants
) {
    public static PostUpdateResponse of(Post post,
                                        List<Position> lookingPositions,
                                        Integer currentParticipants,
                                        PostWriter writer,
                                        List<PostParticipant> participants) {
        return new PostUpdateResponse(
                post.getId(),
                post.getGameMode().name(),
                post.getQueueType(),
                post.getMyPosition(),
                lookingPositions,
                post.getMic(),
                post.getRecruitCount(),
                currentParticipants,
                post.getStatus(),
                post.getMemo(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                writer,
                participants
        );
    }
}
