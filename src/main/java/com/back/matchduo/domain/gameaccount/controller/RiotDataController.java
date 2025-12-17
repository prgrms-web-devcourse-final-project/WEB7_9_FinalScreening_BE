package com.back.matchduo.domain.gameaccount.controller;

import com.back.matchduo.domain.gameaccount.dto.RiotApiDto;
import com.back.matchduo.domain.gameaccount.service.RiotDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Riot API 데이터 조회 Controller
 */
@RestController
@RequestMapping("/api/game-accounts")
@RequiredArgsConstructor
public class RiotDataController {

    private final RiotDataService riotDataService;

    /**
     * 저장된 게임 계정 정보로 Riot API 데이터 조회
     * @param gameAccountId 게임 계정 ID
     * @return Riot API에서 받아온 계정 정보
     */
    @GetMapping("/{gameAccountId}/riot-data")
    public ResponseEntity<RiotApiDto.AccountResponse> getRiotAccountData(
            @PathVariable Long gameAccountId) {
        RiotApiDto.AccountResponse response = riotDataService.getRiotAccountData(gameAccountId);
        return ResponseEntity.ok(response);
    }
}

