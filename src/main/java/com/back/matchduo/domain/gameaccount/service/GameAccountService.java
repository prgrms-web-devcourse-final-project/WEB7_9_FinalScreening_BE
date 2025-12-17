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
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
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
    private final DataDragonService dataDragonService;
    
    private static final String GAME_TYPE_LEAGUE_OF_LEGENDS = "LEAGUE_OF_LEGENDS";
    private static final String GAME_TYPE_LEAGUE_OF_LEGENDS_KR = "리그 오브 레전드";
    private static final String PROFILE_ICON_BASE_URL = "https://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png";

    /**
     * 게임 계정 생성 (닉네임과 태그 저장)
     * 한 게임에 한 가지 계정만 등록 가능
     * @param request 게임 타입, 닉네임, 태그를 포함한 요청 DTO
     * @param userId 인증된 사용자 ID
     * @return 생성된 게임 계정 정보
     */
    public GameAccountResponse createGameAccount(GameAccountCreateRequest request, Long userId) {
        // User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_USER));

        // 중복 체크: 같은 유저가 같은 게임 타입의 계정을 이미 가지고 있는지 확인
        gameAccountRepository.findByUser_IdAndGameType(userId, request.getGameType())
                .ifPresent(existingAccount -> {
                    throw new CustomException(CustomErrorCode.DUPLICATE_GAME_ACCOUNT);
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

        // 소환사 아이콘 조회 (롤만, 계정 생성 시)
        Integer profileIconId = fetchProfileIconId(puuid, request.getGameType());

        // 게임 계정 생성
        GameAccount gameAccount = GameAccount.builder()
                .gameNickname(request.getGameNickname())
                .gameTag(request.getGameTag())
                .gameType(request.getGameType())
                .puuid(puuid)
                .profileIconId(profileIconId)
                .user(user)
                .build();

        GameAccount savedGameAccount = gameAccountRepository.save(gameAccount);

        // 프로필 아이콘 URL 생성
        String profileIconUrl = getProfileIconUrl(savedGameAccount.getProfileIconId());

        return GameAccountResponse.builder()
                .gameAccountId(savedGameAccount.getGameAccountId())
                .gameNickname(savedGameAccount.getGameNickname())
                .gameTag(savedGameAccount.getGameTag())
                .gameType(savedGameAccount.getGameType())
                .puuid(savedGameAccount.getPuuid())
                .profileIconId(profileIconId)
                .profileIconUrl(profileIconUrl)
                .userId(savedGameAccount.getUser().getId())
                .createdAt(savedGameAccount.getCreatedAt())
                .updatedAt(savedGameAccount.getUpdatedAt())
                .build();
    }

    /**
     * 게임 계정 조회
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID
     * @return 게임 계정 정보
     */
    @Transactional(readOnly = true)
    public GameAccountResponse getGameAccount(Long gameAccountId, Long userId) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // 소유자 검증
        if (!gameAccount.getUser().getId().equals(userId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_GAME_ACCOUNT);
        }

        // DB에서 프로필 아이콘 ID 가져오기
        String profileIconUrl = getProfileIconUrl(gameAccount.getProfileIconId());

        return GameAccountResponse.builder()
                .gameAccountId(gameAccount.getGameAccountId())
                .gameNickname(gameAccount.getGameNickname())
                .gameTag(gameAccount.getGameTag())
                .gameType(gameAccount.getGameType())
                .puuid(gameAccount.getPuuid())
                .profileIconId(gameAccount.getProfileIconId())
                .profileIconUrl(profileIconUrl)
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
                .map(gameAccount -> {
                    // DB에서 프로필 아이콘 URL 생성
                    String profileIconUrl = getProfileIconUrl(gameAccount.getProfileIconId());
                    
                    return GameAccountResponse.builder()
                            .gameAccountId(gameAccount.getGameAccountId())
                            .gameNickname(gameAccount.getGameNickname())
                            .gameTag(gameAccount.getGameTag())
                            .gameType(gameAccount.getGameType())
                            .puuid(gameAccount.getPuuid())
                            .profileIconId(gameAccount.getProfileIconId())
                            .profileIconUrl(profileIconUrl)
                            .userId(gameAccount.getUser().getId())
                            .createdAt(gameAccount.getCreatedAt())
                            .updatedAt(gameAccount.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    /**
     * 게임 계정 수정 (닉네임, 태그, puuid 업데이트)
     * @param gameAccountId 게임 계정 ID
     * @param request 수정할 닉네임과 태그
     * @param userId 인증된 사용자 ID
     * @return 수정된 게임 계정 정보
     */
    public GameAccountResponse updateGameAccount(Long gameAccountId, GameAccountUpdateRequest request, Long userId) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // 소유자 검증
        if (!gameAccount.getUser().getId().equals(userId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_GAME_ACCOUNT);
        }

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

        // 프로필 아이콘 갱신 (롤만, 계정 수정 시)
        Integer profileIconId = fetchProfileIconId(puuid, gameAccount.getGameType());

        // 게임 계정 정보 업데이트
        gameAccount.update(request.getGameNickname(), request.getGameTag(), puuid);
        if (profileIconId != null) {
            gameAccount.updateProfileIconId(profileIconId);
        }
        GameAccount updatedGameAccount = gameAccountRepository.save(gameAccount);

        // 프로필 아이콘 URL 생성
        String profileIconUrl = getProfileIconUrl(updatedGameAccount.getProfileIconId());

        return GameAccountResponse.builder()
                .gameAccountId(updatedGameAccount.getGameAccountId())
                .gameNickname(updatedGameAccount.getGameNickname())
                .gameTag(updatedGameAccount.getGameTag())
                .gameType(updatedGameAccount.getGameType())
                .puuid(updatedGameAccount.getPuuid())
                .profileIconId(updatedGameAccount.getProfileIconId())
                .profileIconUrl(profileIconUrl)
                .userId(updatedGameAccount.getUser().getId())
                .createdAt(updatedGameAccount.getCreatedAt())
                .updatedAt(updatedGameAccount.getUpdatedAt())
                .build();
    }

    /**
     * 게임 계정 삭제 (연동 해제)
     * 게임 계정 삭제 시 관련된 랭크 정보도 함께 삭제됩니다.
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID
     */
    public void deleteGameAccount(Long gameAccountId, Long userId) {
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // 소유자 검증
        if (!gameAccount.getUser().getId().equals(userId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_GAME_ACCOUNT);
        }

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

    /**
     * 프로필 아이콘 ID 조회 (공통 메서드)
     * @param puuid PUUID
     * @param gameType 게임 타입
     * @return 프로필 아이콘 ID (조회 실패 시 null)
     */
    private Integer fetchProfileIconId(String puuid, String gameType) {
        if (puuid == null || puuid.isEmpty()) {
            return null;
        }

        if (!GAME_TYPE_LEAGUE_OF_LEGENDS.equals(gameType) && 
            !GAME_TYPE_LEAGUE_OF_LEGENDS_KR.equals(gameType)) {
            return null;
        }

        try {
            RiotApiDto.SummonerResponse summonerResponse = riotApiClient.getSummonerByPuuid(puuid);
            Integer profileIconId = summonerResponse != null ? summonerResponse.getProfileIconId() : null;
            log.debug("소환사 아이콘 조회 성공: profileIconId={}", profileIconId);
            return profileIconId;
        } catch (Exception e) {
            log.warn("소환사 아이콘 조회 실패: puuid={}, error={}", puuid, e.getMessage());
            return null;
        }
    }

    /**
     * 프로필 아이콘 갱신 (랭크 정보 갱신 시 함께 호출)
     * @param gameAccount 게임 계정
     * @return 갱신된 프로필 아이콘 ID (조회 실패 시 null)
     */
    public Integer refreshProfileIconId(GameAccount gameAccount) {
        Integer profileIconId = fetchProfileIconId(gameAccount.getPuuid(), gameAccount.getGameType());
        
        if (profileIconId != null) {
            gameAccount.updateProfileIconId(profileIconId);
            gameAccountRepository.save(gameAccount);
            log.info("프로필 아이콘 갱신 완료: gameAccountId={}, profileIconId={}", 
                    gameAccount.getGameAccountId(), profileIconId);
        }
        
        return profileIconId;
    }

    /**
     * 소환사 아이콘 이미지 URL 생성
     * @param profileIconId 소환사 아이콘 ID
     * @return 완전한 이미지 URL (profileIconId가 null이면 null 반환)
     */
    private String getProfileIconUrl(Integer profileIconId) {
        if (profileIconId == null) {
            return null;
        }

        try {
            String version = dataDragonService.getLatestVersion();
            return String.format(PROFILE_ICON_BASE_URL, version, profileIconId);
        } catch (Exception e) {
            log.warn("소환사 아이콘 URL 생성 실패: profileIconId={}, error={}", profileIconId, e.getMessage());
            return null;
        }
    }
}
