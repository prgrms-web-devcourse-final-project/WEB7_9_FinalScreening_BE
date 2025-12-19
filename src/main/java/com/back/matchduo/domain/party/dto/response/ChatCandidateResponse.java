package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatCandidateResponse {
    private Long userId;
    private String nickname;
    private String profileImage;

    public static ChatCandidateResponse from(User user) {
        return ChatCandidateResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}