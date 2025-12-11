package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.party.entity.PartyMemberRole;

import java.time.LocalDateTime;
import java.util.List;

public record PartyMemberListResponse(
        Long partyId,
        int currentCount,
        int maxCount,
        List<PartyMemberDto> members
) {
    public record PartyMemberDto(
            Long partyMemberId,
            Long userId,
            String nickname,
            String profileImage,
            PartyMemberRole role, // LEADER / MEMBER
            LocalDateTime joinedAt
    ) {
        public static PartyMemberDto of(Long partyMemberId, Long userId, PartyMemberRole role, LocalDateTime joinedAt, String nickname, String profileImage) {
            return new PartyMemberDto(
                    partyMemberId,
                    userId,
                    nickname,
                    profileImage,
                    role,
                    joinedAt
            );
        }
    }
}