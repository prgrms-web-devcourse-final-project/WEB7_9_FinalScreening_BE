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

public record PostListResponse(
        List<PostDto> posts,
        Long nextCursor,
        Boolean hasNext
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PostListResponse of(List<Post> posts, Long nextCursor, Boolean hasNext, 
                                      java.util.function.Function<Post, PostWriter> writerMapper) {
        List<PostDto> postDtos = posts.stream()
                .map(post -> PostDto.of(post, writerMapper.apply(post)))
                .toList();

        return new PostListResponse(postDtos, nextCursor, hasNext);
    }

    public record PostDto(
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
        public static PostDto of(Post post, PostWriter writer) {
            return new PostDto(
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
}
