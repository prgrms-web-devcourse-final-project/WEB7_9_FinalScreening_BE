package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.response.RankResponse;
import com.back.matchduo.domain.gameaccount.service.RankService;
import com.back.matchduo.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    /**
     * 게임 계정의 랭크 정보 갱신 (전적 갱신)
     * @param gameAccountId 게임 계정 ID
     * @return 갱신된 랭크 정보 목록
     */
    @PostMapping("/{gameAccountId}/ranks/refresh")
    public ResponseEntity<List<RankResponse>> refreshRankData(
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        List<RankResponse> responses = rankService.refreshRankData(gameAccountId, userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 게임 계정의 모든 랭크 정보 조회
     * @param gameAccountId 게임 계정 ID
     * @return 랭크 정보 목록
     */
    @GetMapping("/{gameAccountId}/ranks")
    public ResponseEntity<List<RankResponse>> getRanksByGameAccountId(
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        List<RankResponse> responses = rankService.getRanksByGameAccountId(gameAccountId, userId);
        return ResponseEntity.ok(responses);
    }
}

