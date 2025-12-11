package com.back.matchduo.domain.party.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PartyMemberAddRequest(
        @NotEmpty(message = "추가할 유저를 최소 1명 이상 선택해주세요.")
        List<Long> targetUserIds // 다인파티 확정성 고려하여 List로 받음
) {}