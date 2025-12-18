package com.back.matchduo.domain.post.dto.response;

public record PostParticipant(
        Long userId,
        String communityNickname,
        String communityProfileImageUrl,
        String role // LEADER / MEMBER
) {
}
