package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.request.GameAccountCreateRequest;
import com.back.matchduo.domain.gameaccount.dto.request.GameAccountUpdateRequest;
import com.back.matchduo.domain.gameaccount.dto.response.GameAccountDeleteResponse;
import com.back.matchduo.domain.gameaccount.dto.response.GameAccountResponse;
import com.back.matchduo.domain.gameaccount.dto.response.RefreshAllResponse;
import com.back.matchduo.domain.gameaccount.service.GameAccountService;
import com.back.matchduo.global.security.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
public class GameAccountController {

    private final GameAccountService gameAccountService;

    /**
     * 게임 계정 생성 (닉네임과 태그 저장)
     * @param request 닉네임, 태그를 포함한 요청 DTO
     * @return 생성된 게임 계정 정보
     */
    @PostMapping
    public ResponseEntity<GameAccountResponse> createGameAccount(
            @Valid @RequestBody GameAccountCreateRequest request) {
        Long userId = AuthPrincipal.getUserId();
        GameAccountResponse response = gameAccountService.createGameAccount(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자의 모든 게임 계정 조회
     * @return 게임 계정 목록
     */
    @GetMapping
    public ResponseEntity<List<GameAccountResponse>> getUserGameAccounts() {
        Long userId = AuthPrincipal.getUserId();
        List<GameAccountResponse> responses = gameAccountService.getUserGameAccounts(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 게임 계정 조회
     * @param gameAccountId 게임 계정 ID
     * @return 게임 계정 정보
     */
    @GetMapping("/{gameAccountId}")
    public ResponseEntity<GameAccountResponse> getGameAccount(
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        GameAccountResponse response = gameAccountService.getGameAccount(gameAccountId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게임 계정 수정 (연동 수정)
     * @param gameAccountId 게임 계정 ID
     * @param request 수정할 닉네임과 태그
     * @return 수정된 게임 계정 정보
     */
    @PutMapping("/{gameAccountId}")
    public ResponseEntity<GameAccountResponse> updateGameAccount(
            @PathVariable Long gameAccountId,
            @Valid @RequestBody GameAccountUpdateRequest request) {
        Long userId = AuthPrincipal.getUserId();
        GameAccountResponse response = gameAccountService.updateGameAccount(gameAccountId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게임 계정 삭제 (연동 해제)
     * @param gameAccountId 게임 계정 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{gameAccountId}")
    public ResponseEntity<GameAccountDeleteResponse> deleteGameAccount(
            @PathVariable Long gameAccountId) {
        Long userId = AuthPrincipal.getUserId();
        gameAccountService.deleteGameAccount(gameAccountId, userId);
        return ResponseEntity.ok(GameAccountDeleteResponse.builder()
                .message("게임 계정 연동이 해제되었습니다.")
                .gameAccountId(gameAccountId)
                .build());
    }

    /**
     * 게임 계정의 랭크 정보와 매치 정보를 함께 갱신 (통합 전적 갱신)
     * @param gameAccountId 게임 계정 ID
     * @param matchCount 조회할 매치 개수 (기본값: 20)
     * @return 갱신된 랭크 정보와 매치 정보
     */
    @PostMapping("/{gameAccountId}/refresh-all")
    public ResponseEntity<RefreshAllResponse> refreshAll(
            @PathVariable Long gameAccountId,
            @RequestParam(defaultValue = "20") int matchCount) {
        Long userId = AuthPrincipal.getUserId();
        RefreshAllResponse response = gameAccountService.refreshAll(gameAccountId, userId, matchCount);
        return ResponseEntity.ok(response);
    }
}
