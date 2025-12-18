package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.party.entity.PartyMember;
import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.party.entity.PartyMemberState;

import java.time.LocalDateTime;

public record PartyMemberAddResponse(
        Long partyMemberId,
        Long userId,
        String nickname,
        String profileImage,
        PartyMemberRole role,// LEADER / MEMBER
        PartyMemberState state,// JOINED
        LocalDateTime joinedAt
) {
    public static PartyMemberAddResponse of(PartyMember member, String nickname, String profileImage) {
        return new PartyMemberAddResponse(
                member.getId(),
                member.getUser().getId(),
                nickname,
                profileImage,
                member.getRole(),
                member.getState(),
                member.getJoinedAt()
        );
    }
}