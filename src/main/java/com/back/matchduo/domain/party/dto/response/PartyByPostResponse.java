package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.party.entity.PartyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PartyByPostResponse(
        Long partyId,
        Long postId,
        PartyStatus status,
        int currentCount,
        int maxCount,
        LocalDateTime createdAt,
        boolean isJoined,
        List<PartyMemberDto> members
) {
    public record PartyMemberDto(
            Long partyMemberId,
            Long userId,
            String nickname,
            String profileImage,
            PartyMemberRole role
    ) {
        public static PartyMemberDto of(Long partyMemberId, Long userId, String nickname, String profileImage, PartyMemberRole role) {
            return new PartyMemberDto(partyMemberId, userId, nickname, profileImage, role);
        }
    }
}