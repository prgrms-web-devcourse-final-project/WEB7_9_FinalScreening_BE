package com.back.matchduo.domain.party.dto.response;

import com.back.matchduo.domain.party.entity.PartyMemberRole;
import com.back.matchduo.domain.party.entity.PartyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MyPartyListResponse(
        List<MyPartyDto> parties
) {
    public record MyPartyDto(
            Long partyId,
            Long gameModeId,
            Long postId,
            String postTitle,
            String gameMode,
            PartyStatus status,
            PartyMemberRole myRole,
            LocalDateTime joinedAt
    ) {
        public static MyPartyDto of(Long partyId,Long gameModeId, Long postId, String postTitle, String gameMode, PartyStatus status, PartyMemberRole myRole, LocalDateTime joinedAt) {
            return new MyPartyDto(
                    partyId,
                    gameModeId,
                    postId,
                    postTitle,
                    gameMode,
                    status,
                    myRole,
                    joinedAt
            );
        }
    }
}