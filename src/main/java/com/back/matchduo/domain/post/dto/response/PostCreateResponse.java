package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;

import java.time.LocalDateTime;
import java.util.List;

public record PostCreateResponse(
        Long postId,
        Long partyId,
        Long gameAccountId,
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
    public static PostCreateResponse of(Post post,
                                        Long partyId,
                                        Long gameAccountId,
                                        List<Position> lookingPositions,
                                        Integer currentParticipants,
                                        PostWriter writer,
                                        List<PostParticipant> participants) {
        return new PostCreateResponse(
                post.getId(),
                partyId,
                gameAccountId,
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
                writer,
                participants
        );
    }
}
