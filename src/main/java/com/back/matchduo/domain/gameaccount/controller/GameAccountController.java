package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.GameAccountDto;
import com.back.matchduo.domain.gameaccount.service.GameAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
public class GameAccountController {

    private final GameAccountService gameAccountService;

    /**
     * 게임 계정 생성 (닉네임과 태그 저장)
     * @param request 닉네임, 태그, 유저 ID를 포함한 요청 DTO
     * @return 생성된 게임 계정 정보
     */
    @PostMapping
    public ResponseEntity<GameAccountDto.Response> createGameAccount(
            @Valid @RequestBody GameAccountDto.CreateRequest request) {
        GameAccountDto.Response response = gameAccountService.createGameAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게임 계정 조회
     * @param gameAccountId 게임 계정 ID
     * @return 게임 계정 정보
     */
    @GetMapping("/{gameAccountId}")
    public ResponseEntity<GameAccountDto.Response> getGameAccount(
            @PathVariable Long gameAccountId) {
        GameAccountDto.Response response = gameAccountService.getGameAccount(gameAccountId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게임 계정 삭제 (연동 해제)
     * @param gameAccountId 게임 계정 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/{gameAccountId}")
    public ResponseEntity<GameAccountDto.DeleteResponse> deleteGameAccount(
            @PathVariable Long gameAccountId) {
        gameAccountService.deleteGameAccount(gameAccountId);
        return ResponseEntity.ok(GameAccountDto.DeleteResponse.builder()
                .message("게임 계정 연동이 해제되었습니다.")
                .gameAccountId(gameAccountId)
                .build());
    }
}
