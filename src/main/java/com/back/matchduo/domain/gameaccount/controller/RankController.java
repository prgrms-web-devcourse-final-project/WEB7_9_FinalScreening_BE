package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.response.RankResponse;
import com.back.matchduo.domain.gameaccount.service.RankService;
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
@Tag(name = "게임 계정 - 랭크", description = "게임 계정의 랭크 정보 조회 및 갱신 API")
public class RankController {

    private final RankService rankService;

    /**
     * 게임 계정의 랭크 정보 갱신 (전적 갱신)
     * @param gameAccountId 게임 계정 ID
     * @return 갱신된 랭크 정보 목록
     */
    @PostMapping("/{gameAccountId}/ranks/refresh")
    @Operation(
            summary = "랭크 정보 갱신",
            description = "Riot API를 호출하여 게임 계정의 최신 랭크 정보를 가져와 DB에 저장합니다. 솔로랭크와 자유랭크 정보를 모두 갱신합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랭크 정보 갱신 성공"),
            @ApiResponse(responseCode = "404", description = "게임 계정을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "게임 계정에 puuid가 없습니다. 먼저 게임 계정을 등록해주세요."),
            @ApiResponse(responseCode = "500", description = "랭크 정보를 가져오는데 실패했습니다.")
    })
    public ResponseEntity<List<RankResponse>> refreshRankData(
            @Parameter(description = "게임 계정 ID", required = true)
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
    @Operation(
            summary = "랭크 정보 조회",
            description = "DB에 저장된 게임 계정의 랭크 정보를 조회합니다. 솔로랭크와 자유랭크 정보를 모두 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랭크 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게임 계정을 찾을 수 없습니다.")
    })
    public ResponseEntity<List<RankResponse>> getRanksByGameAccountId(
            @Parameter(description = "게임 계정 ID", required = true)
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        List<RankResponse> responses = rankService.getRanksByGameAccountId(gameAccountId, userId);
        return ResponseEntity.ok(responses);
    }
}

