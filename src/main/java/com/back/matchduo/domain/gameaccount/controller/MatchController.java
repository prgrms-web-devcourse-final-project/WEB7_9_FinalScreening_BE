package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.response.FavoriteChampionResponse;
import com.back.matchduo.domain.gameaccount.dto.response.MatchResponse;
import com.back.matchduo.domain.gameaccount.service.MatchService;
import com.back.matchduo.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
@Tag(name = "게임 계정 - 매치", description = "게임 계정의 매치(전적) 정보 조회 및 갱신 API")
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
    @Operation(
            summary = "매치 정보 갱신",
            description = "Riot API를 호출하여 게임 계정의 최근 매치 정보를 가져와 DB에 저장합니다. 이미 저장된 매치는 중복 저장되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매치 정보 갱신 성공"),
            @ApiResponse(responseCode = "404", description = "게임 계정을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "게임 계정에 puuid가 없습니다. 먼저 게임 계정을 등록해주세요."),
            @ApiResponse(responseCode = "500", description = "매치 정보를 가져오는데 실패했습니다.")
    })
    public ResponseEntity<List<MatchResponse>> refreshMatchHistory(
            @Parameter(description = "게임 계정 ID", required = true)
            @PathVariable Long gameAccountId,
            @Parameter(description = "조회할 매치 개수 (기본값: 20, 최대: 100)")
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
    @Operation(
            summary = "최근 매치 조회",
            description = "DB에 저장된 게임 계정의 최근 매치 정보를 조회합니다. 최신순으로 정렬되어 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매치 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임 계정을 찾을 수 없습니다.")
    })
    public ResponseEntity<List<MatchResponse>> getRecentMatches(
            @Parameter(description = "게임 계정 ID", required = true)
            @PathVariable Long gameAccountId,
            @Parameter(description = "조회할 매치 개수 (기본값: 20)")
            @RequestParam(defaultValue = "20") int count) {
        Long userId = AuthPrincipal.getUserId();
        List<MatchResponse> responses = matchService.getRecentMatches(gameAccountId, userId, count);
        return ResponseEntity.ok(responses);
    }

    /**
     * 선호 챔피언 TOP 3 조회
     * 누구나 다른 사람의 게임 계정 선호 챔피언도 조회할 수 있습니다.
     * @param gameAccountId 게임 계정 ID
     * @return 선호 챔피언 목록 (최대 3개)
     */
    @GetMapping("/{gameAccountId}/champions/favorite")
    @Operation(
            summary = "선호 챔피언 조회",
            description = "DB에 저장된 게임 계정의 최근 20게임 기준 선호 챔피언 TOP 3를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선호 챔피언 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임 계정을 찾을 수 없습니다.")
    })
    public ResponseEntity<List<FavoriteChampionResponse>> getFavoriteChampions(
            @Parameter(description = "게임 계정 ID", required = true)
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        List<FavoriteChampionResponse> responses = matchService.getFavoriteChampions(gameAccountId, userId);
        return ResponseEntity.ok(responses);
    }
}

