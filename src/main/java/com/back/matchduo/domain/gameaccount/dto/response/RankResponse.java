package com.back.matchduo.domain.gameaccount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankResponse {
    private Long rankId;
    private String queueType; // RANKED_SOLO_5x5, RANKED_FLEX_SR
    private String tier; // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER
    private String rank; // I, II, III, IV
    private Integer wins;
    private Integer losses;
    private Double winRate; // 승률
    private Long gameAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

