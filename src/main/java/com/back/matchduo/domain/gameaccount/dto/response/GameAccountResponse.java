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
public class GameAccountResponse {
    private Long gameAccountId;
    private String gameNickname;
    private String gameTag;
    private String gameType;
    private String puuid;
    private Integer profileIconId; // 소환사 아이콘 ID
    private String profileIconUrl; // 소환사 아이콘 이미지 URL
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

