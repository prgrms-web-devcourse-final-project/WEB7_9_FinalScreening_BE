package com.back.matchduo.domain.party.dto.response;

import java.time.LocalDateTime;

public record PartyCloseResponse(
        Long partyId,
        String status, // "CLOSED"
        LocalDateTime closedAt
) {}