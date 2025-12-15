package com.back.matchduo.domain.post.dto.response;

import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

public record PostCreateResponse(
        Long postId,
        Long gameModeId,
        String gameMode,
        QueueType queueType,
        Position myPosition,
        List<Position> lookingPositions,
        Boolean mic,
        Integer recruitCount,
        PostStatus status,
        String memo,
        LocalDateTime createdAt,
        PostWriter writer
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PostCreateResponse of(Post post, PostWriter writer) {
        return new PostCreateResponse(
                post.getId(),
                post.getGameMode().getId(),
                post.getGameMode().getModeCode(),
                post.getQueueType(),
                post.getMyPosition(),
                parsePositions(post.getLookingPositions()),
                post.getMic(),
                post.getRecruitCount(),
                post.getStatus(),
                post.getMemo(),
                post.getCreatedAt(),
                writer
        );
    }

    private static List<Position> parsePositions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Position>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
