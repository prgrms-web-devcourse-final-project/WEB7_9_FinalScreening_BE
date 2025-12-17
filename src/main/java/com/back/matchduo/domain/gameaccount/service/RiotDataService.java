package com.back.matchduo.domain.gameaccount.service;

import com.back.matchduo.domain.gameaccount.client.RiotApiClient;
import com.back.matchduo.domain.gameaccount.dto.RiotApiDto;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Riot API 데이터를 처리하는 Service
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RiotDataService {

    private final GameAccountRepository gameAccountRepository;
    private final RiotApiClient riotApiClient;

    /**
     * 저장된 게임 계정 정보로 Riot API 호출
     * @param gameAccountId 게임 계정 ID
     * @return Riot API에서 받아온 계정 정보
     */
    @Transactional(readOnly = true)
    public RiotApiDto.AccountResponse getRiotAccountData(Long gameAccountId) {
        // DB에서 게임 계정 정보 조회
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new IllegalArgumentException("게임 계정을 찾을 수 없습니다. gameAccountId: " + gameAccountId));

        // 저장된 닉네임과 태그로 Riot API 호출
        String gameName = gameAccount.getGameNickname();
        String tagLine = gameAccount.getGameTag();

        log.info("Riot API 호출: gameName={}, tagLine={}", gameName, tagLine);

        // Riot API 호출
        RiotApiDto.AccountResponse accountResponse = riotApiClient.getAccountByRiotId(gameName, tagLine);

        return accountResponse;
    }
}

