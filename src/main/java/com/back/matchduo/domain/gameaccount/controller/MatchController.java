package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.response.MatchResponse;
import com.back.matchduo.domain.gameaccount.service.MatchService;
import com.back.matchduo.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    private static final int DEFAULT_MATCH_COUNT = 20;

    /**
     * 전적 갱신 (Riot API 호출 → DB 저장)
     * 누구나 다른 사람의 게임 계정 전적도 갱신할 수 있습니다.
     * @param gameAccountId 게임 계정 ID
     * @param count 조회할 매치 개수 (기본값: 20)
     * @return 저장된 매치 정보 목록
     */
    @PostMapping("/{gameAccountId}/matches/refresh")
    public ResponseEntity<List<MatchResponse>> refreshMatchHistory(
            @PathVariable Long gameAccountId,
            @RequestParam(defaultValue = "20") int count) {
        Long userId = AuthPrincipal.getUserId();
        List<MatchResponse> responses = matchService.refreshMatchHistory(gameAccountId, userId, count);
        return ResponseEntity.ok(responses);
    }

    /**
     * 최근 매치 조회 (DB에서 조회)
     * 누구나 다른 사람의 게임 계정 전적도 조회할 수 있습니다.
     * @param gameAccountId 게임 계정 ID
     * @param count 조회할 매치 개수 (기본값: 20)
     * @return 매치 정보 목록
     */
    @GetMapping("/{gameAccountId}/matches")
    public ResponseEntity<List<MatchResponse>> getRecentMatches(
            @PathVariable Long gameAccountId,
            @RequestParam(defaultValue = "20") int count) {
        Long userId = AuthPrincipal.getUserId();
        List<MatchResponse> responses = matchService.getRecentMatches(gameAccountId, userId, count);
        return ResponseEntity.ok(responses);
    }
}

