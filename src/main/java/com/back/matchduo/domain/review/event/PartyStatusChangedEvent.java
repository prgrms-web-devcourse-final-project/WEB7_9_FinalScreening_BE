package com.back.matchduo.domain.review.event;

import com.back.matchduo.domain.party.entity.PartyStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartyStatusChangedEvent {
    private Long partyId;
    private PartyStatus prevStatus;
    private PartyStatus newStatus;
}