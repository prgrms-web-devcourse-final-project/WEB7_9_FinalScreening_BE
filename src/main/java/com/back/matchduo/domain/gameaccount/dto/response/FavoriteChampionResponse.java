package com.back.matchduo.domain.gameaccount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteChampionResponse {
    private Integer rank;  // 순위 (1, 2, 3)
    private Integer championId;  // 챔피언 ID
    private String championName;  // 챔피언 이름
    private String championImageUrl;  // 챔피언 이미지 URL
    private Integer totalGames;  // 총 게임 수 (최근 20게임 중)
    private Integer wins;  // 승리 횟수
    private Integer losses;  // 패배 횟수
    private Double winRate;  // 승률 (%)
}

