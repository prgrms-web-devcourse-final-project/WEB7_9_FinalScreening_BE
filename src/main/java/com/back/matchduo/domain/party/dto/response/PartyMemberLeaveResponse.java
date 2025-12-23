package com.back.matchduo.domain.party.dto.response;

import java.time.LocalDateTime;

public record PartyMemberLeaveResponse(
        Long partyId,
        Long memberId,
        LocalDateTime leftAt
) {
    public static PartyMemberLeaveResponse of(Long partyId, Long memberId) {
        return new PartyMemberLeaveResponse(
                partyId,
                memberId,
                LocalDateTime.now()
        );
    }
}