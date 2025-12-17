package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.party.entity.PartyMember;
import java.time.LocalDateTime;

public record PartyMemberRemoveResponse(
        Long partyMemberId,
        Long userId,
        String state,// "LEFT"
        LocalDateTime leftAt
) {
    public static PartyMemberRemoveResponse from(PartyMember member) {
        return new PartyMemberRemoveResponse(
                member.getId(),
                member.getUser().getId(),
                member.getState().name(),
                member.getLeftAt()
        );
    }
}