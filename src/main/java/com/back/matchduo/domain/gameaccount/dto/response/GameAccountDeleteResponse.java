package com.back.matchduo.domain.gameaccount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAccountDeleteResponse {
    private String message;
    private Long gameAccountId;
}

