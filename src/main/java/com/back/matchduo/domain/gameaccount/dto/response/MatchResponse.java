package com.back.matchduo.domain.gameaccount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {
    private String matchId;
    private Integer queueId;
    private Long gameStartTimestamp;  // 원시 데이터 (밀리초)
    private String gameStartTimeFormatted;  // 포맷팅된 시간 (예: "2024-01-15 14:30" 또는 "2시간 전")
    private Integer gameDuration;  // 원시 데이터 (초)
    private String gameDurationFormatted;  // 포맷팅된 시간 (예: "25분 30초" 또는 "25:30")
    private Boolean win;

    // 플레이어 정보
    private Integer championId;
    private String championName;
    private String championImageUrl;
    private Integer spell1Id;
    private String spell1ImageUrl;
    private Integer spell2Id;
    private String spell2ImageUrl;
    private String perks;  // JSON 문자열
    private List<String> perkImageUrls;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Double kda;
    private Integer cs;
    private Integer level;
    private List<Integer> items;  // [item0, item1, item2, item3, item4, item5, item6]
    private List<String> itemImageUrls;  // [url0, url1, url2, url3, url4, url5, url6]
}

