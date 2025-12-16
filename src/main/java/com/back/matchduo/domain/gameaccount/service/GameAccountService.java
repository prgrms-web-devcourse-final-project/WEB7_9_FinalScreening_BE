package com.back.matchduo.domain.gameaccount.service;

import com.back.matchduo.domain.gameaccount.client.RiotApiClient;
import com.back.matchduo.domain.gameaccount.dto.request.GameAccountCreateRequest;
import com.back.matchduo.domain.gameaccount.dto.request.GameAccountUpdateRequest;
import com.back.matchduo.domain.gameaccount.dto.response.GameAccountResponse;
import com.back.matchduo.domain.gameaccount.dto.RiotApiDto;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.gameaccount.repository.RankRepository;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GameAccountService {

    private final GameAccountRepository gameAccountRepository;
    private final UserRepository userRepository;
    private final RiotApiClient riotApiClient;
    private final RankRepository rankRepository;

    /**
     * 게임 계정 생성 (닉네임과 태그 저장)
     * 한 게임에 한 가지 계정만 등록 가능
     * @param request 게임 타입, 닉네임, 태그, 유저 ID를 포함한 요청 DTO
     * @return 생성된 게임 계정 정보
     */
    public GameAccountResponse createGameAccount(GameAccountCreateRequest request) {
        // 임시: User 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. userId: " + request.getUserId()));

        // 중복 체크: 같은 유저가 같은 게임 타입의 계정을 이미 가지고 있는지 확인
        gameAccountRepository.findByUser_IdAndGameType(request.getUserId(), request.getGameType())
                .ifPresent(existingAccount -> {
                    throw new IllegalArgumentException(
                        String.format("이미 %s 게임 계정이 등록되어 있습니다. (기존 계정 ID: %d)", 
                            request.getGameType(), existingAccount.getGameAccountId())
                    );
                });

        // Riot API 호출하여 puuid 가져오기
        String puuid = null;
        try {
            RiotApiDto.AccountResponse accountResponse = riotApiClient.getAccountByRiotId(
                    request.getGameNickname(), 
                    request.getGameTag()
            );
            puuid = accountResponse != null ? accountResponse.getPuuid() : null;
            log.info("Riot API 호출 성공: puuid={}", puuid != null ? "조회됨" : "null");
        } catch (Exception e) {
            log.warn("Riot API 호출 실패: gameNickname={}, gameTag={}, error={}. puuid 없이 계정 생성합니다.", 
                    request.getGameNickname(), request.getGameTag(), e.getMessage());
            // Riot API 호출 실패 시에도 계정은 생성 (puuid는 null)
        }

        // 게임 계정 생성
        GameAccount gameAccount = GameAccount.builder()
                .gameNickname(request.getGameNickname())
                .gameTag(request.getGameTag())
                .gameType(request.getGameType())
                .puuid(puuid)
                .user(user)
                .build();

        GameAccount savedGameAccount = gameAccountRepository.save(gameAccount);

        return GameAccountResponse.builder()
                .gameAccountId(savedGameAccount.getGameAccountId())
                .gameNickname(savedGameAccount.getGameNickname())
                .gameTag(savedGameAccount.getGameTag())
                .gameType(savedGameAccount.getGameType())
                .puuid(savedGameAccount.getPuuid())
                .userId(savedGameAccount.getUser().getId())
                .createdAt(savedGameAccount.getCreatedAt())
                .updatedAt(savedGameAccount.getUpdatedAt())
                .build();
    }

    /**
     * 게임 계정 조회
     * @param gameAccountId 게임 계정 ID
     * @return 게임 계정 정보
     */
    @Transactional(readOnly = true)
    public GameAccountResponse getGameAccount(Long gameAccountId) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new IllegalArgumentException("게임 계정을 찾을 수 없습니다. gameAccountId: " + gameAccountId));

        return GameAccountResponse.builder()
                .gameAccountId(gameAccount.getGameAccountId())
                .gameNickname(gameAccount.getGameNickname())
                .gameTag(gameAccount.getGameTag())
                .gameType(gameAccount.getGameType())
                .puuid(gameAccount.getPuuid())
                .userId(gameAccount.getUser().getId())
                .createdAt(gameAccount.getCreatedAt())
                .updatedAt(gameAccount.getUpdatedAt())
                .build();
    }

    /**
     * 사용자의 모든 게임 계정 조회
     * @param userId 유저 ID
     * @return 게임 계정 목록
     */
    @Transactional(readOnly = true)
    public List<GameAccountResponse> getUserGameAccounts(Long userId) {
        List<GameAccount> gameAccounts = gameAccountRepository.findByUser_Id(userId);
        
        return gameAccounts.stream()
                .map(gameAccount -> GameAccountResponse.builder()
                        .gameAccountId(gameAccount.getGameAccountId())
                        .gameNickname(gameAccount.getGameNickname())
                        .gameTag(gameAccount.getGameTag())
                        .gameType(gameAccount.getGameType())
                        .puuid(gameAccount.getPuuid())
                        .userId(gameAccount.getUser().getId())
                        .createdAt(gameAccount.getCreatedAt())
                        .updatedAt(gameAccount.getUpdatedAt())
                        .build())
                .toList();
    }

    /**
     * 게임 계정 수정 (닉네임, 태그, puuid 업데이트)
     * @param gameAccountId 게임 계정 ID
     * @param request 수정할 닉네임과 태그
     * @return 수정된 게임 계정 정보
     */
    public GameAccountResponse updateGameAccount(Long gameAccountId, GameAccountUpdateRequest request) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new IllegalArgumentException("게임 계정을 찾을 수 없습니다. gameAccountId: " + gameAccountId));

        // Riot API 호출하여 새로운 puuid 가져오기
        String puuid = null;
        try {
            RiotApiDto.AccountResponse accountResponse = riotApiClient.getAccountByRiotId(
                    request.getGameNickname(),
                    request.getGameTag()
            );
            puuid = accountResponse != null ? accountResponse.getPuuid() : null;
            log.info("Riot API 호출 성공: puuid={}", puuid != null ? "조회됨" : "null");
        } catch (Exception e) {
            log.warn("Riot API 호출 실패: gameNickname={}, gameTag={}, error={}. puuid는 기존 값을 유지합니다.", 
                    request.getGameNickname(), request.getGameTag(), e.getMessage());
            // Riot API 호출 실패 시 기존 puuid 유지
            puuid = gameAccount.getPuuid();
        }

        // 게임 계정 정보 업데이트
        gameAccount.update(request.getGameNickname(), request.getGameTag(), puuid);
        GameAccount updatedGameAccount = gameAccountRepository.save(gameAccount);

        return GameAccountResponse.builder()
                .gameAccountId(updatedGameAccount.getGameAccountId())
                .gameNickname(updatedGameAccount.getGameNickname())
                .gameTag(updatedGameAccount.getGameTag())
                .gameType(updatedGameAccount.getGameType())
                .puuid(updatedGameAccount.getPuuid())
                .userId(updatedGameAccount.getUser().getId())
                .createdAt(updatedGameAccount.getCreatedAt())
                .updatedAt(updatedGameAccount.getUpdatedAt())
                .build();
    }

    /**
     * 게임 계정 삭제 (연동 해제)
     * 게임 계정 삭제 시 관련된 랭크 정보도 함께 삭제됩니다.
     * @param gameAccountId 게임 계정 ID
     */
    public void deleteGameAccount(Long gameAccountId) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new IllegalArgumentException("게임 계정을 찾을 수 없습니다. gameAccountId: " + gameAccountId));

        // 관련된 랭크 정보 먼저 삭제
        rankRepository.findByGameAccount_GameAccountId(gameAccountId).forEach(rank -> {
            rankRepository.delete(rank);
            log.debug("랭크 정보 삭제: rankId={}, queueType={}", rank.getRankId(), rank.getQueueType());
        });

        // 게임 계정 삭제
        gameAccountRepository.delete(gameAccount);
        log.info("게임 계정 삭제 완료: gameAccountId={}, gameNickname={}, gameTag={}", 
                gameAccountId, gameAccount.getGameNickname(), gameAccount.getGameTag());
    }
}
