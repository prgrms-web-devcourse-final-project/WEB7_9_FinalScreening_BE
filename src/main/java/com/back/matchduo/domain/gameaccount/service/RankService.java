package com.back.matchduo.domain.gameaccount.service;

import com.back.matchduo.domain.gameaccount.client.RiotApiClient;
import com.back.matchduo.domain.gameaccount.dto.RiotApiDto;
import com.back.matchduo.domain.gameaccount.dto.response.RankResponse;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.entity.Rank;
import com.back.matchduo.domain.gameaccount.repository.GameAccountRepository;
import com.back.matchduo.domain.gameaccount.repository.RankRepository;
import com.back.matchduo.global.exeption.CustomErrorCode;
import com.back.matchduo.global.exeption.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;
    private final GameAccountRepository gameAccountRepository;
    private final RiotApiClient riotApiClient;
    private final GameAccountService gameAccountService;

    /**
     * 승률 계산 (소수점 첫째 자리까지 반올림)
     * @param wins 승수
     * @param losses 패수
     * @return 승률 (wins / (wins + losses) * 100, 소수점 첫째 자리)
     */
    private Double calculateWinRate(Integer wins, Integer losses) {
        int totalGames = wins + losses;
        if (totalGames == 0) {
            return 0.0;
        }
        // 소수점 첫째 자리까지 반올림: 48.65% → 48.7%
        double winRate = (double) wins / totalGames * 100.0;
        return Math.round(winRate * 10.0) / 10.0;
    }

    /**
     * 게임 계정의 랭크 정보 갱신 (전적 갱신)
     * 누구나 다른 사람의 게임 계정 전적도 갱신할 수 있습니다.
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID (로그용)
     * @return 갱신된 랭크 정보 목록
     */
    public List<RankResponse> refreshRankData(Long gameAccountId, Long userId) {
        // 게임 계정 조회
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        // puuid가 없으면 랭크 정보를 가져올 수 없음
        if (gameAccount.getPuuid() == null || gameAccount.getPuuid().isEmpty()) {
            throw new CustomException(CustomErrorCode.GAME_ACCOUNT_NO_PUUID);
        }

        // Riot API 호출하여 랭크 정보 가져오기
        List<RiotApiDto.RankResponse> rankResponses;
        try {
            rankResponses = riotApiClient.getRankByPuuid(gameAccount.getPuuid());
            log.info("Riot API 호출 성공: gameAccountId={}, 랭크 정보 개수={}", gameAccountId, rankResponses != null ? rankResponses.size() : 0);
        } catch (Exception e) {
            log.error("Riot API 호출 실패: gameAccountId={}, error={}", gameAccountId, e.getMessage());
            throw new CustomException(CustomErrorCode.RANK_FETCH_FAILED);
        }

        if (rankResponses == null || rankResponses.isEmpty()) {
            log.warn("랭크 정보가 없습니다: gameAccountId={}", gameAccountId);
            return List.of();
        }

        // 각 랭크 정보를 저장 또는 업데이트
        List<RankResponse> savedRanks = rankResponses.stream()
                .map(riotRank -> {
                    // 승률 계산
                    Double winRate = calculateWinRate(riotRank.getWins(), riotRank.getLosses());

                    // 기존 랭크 정보 조회 (같은 큐 타입)
                    Rank existingRank = rankRepository
                            .findByGameAccount_GameAccountIdAndQueueType(gameAccountId, riotRank.getQueueType())
                            .orElse(null);

                    if (existingRank != null) {
                        // 기존 랭크 정보 업데이트
                        existingRank.update(
                                riotRank.getTier(),
                                riotRank.getRank(),
                                riotRank.getWins(),
                                riotRank.getLosses(),
                                winRate
                        );
                        Rank updatedRank = rankRepository.save(existingRank);
                        return convertToResponse(updatedRank);
                    } else {
                        // 새로운 랭크 정보 생성
                        Rank newRank = Rank.builder()
                                .queueType(riotRank.getQueueType())
                                .tier(riotRank.getTier())
                                .rank(riotRank.getRank())
                                .wins(riotRank.getWins())
                                .losses(riotRank.getLosses())
                                .winRate(winRate)
                                .gameAccount(gameAccount)
                                .build();
                        Rank savedRank = rankRepository.save(newRank);
                        return convertToResponse(savedRank);
                    }
                })
                .collect(Collectors.toList());

        // 프로필 아이콘도 함께 갱신
        gameAccountService.refreshProfileIconId(gameAccount);

        log.info("랭크 정보 갱신 완료: gameAccountId={}, 요청자 userId={}, 소유자 userId={}, 갱신된 랭크 개수={}", 
                gameAccountId, userId, gameAccount.getUser().getId(), savedRanks.size());
        return savedRanks;
    }

    /**
     * 게임 계정의 모든 랭크 정보 조회
     * 누구나 다른 사람의 게임 계정 랭크 정보도 조회할 수 있습니다.
     * @param gameAccountId 게임 계정 ID
     * @param userId 인증된 사용자 ID (로그용)
     * @return 랭크 정보 목록
     */
    @Transactional(readOnly = true)
    public List<RankResponse> getRanksByGameAccountId(Long gameAccountId, Long userId) {
        // 게임 계정 조회
        GameAccount gameAccount = gameAccountRepository.findById(gameAccountId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GAME_ACCOUNT_NOT_FOUND));

        List<Rank> ranks = rankRepository.findByGameAccount_GameAccountId(gameAccountId);
        return ranks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rank 엔티티를 RankResponse로 변환
     */
    private RankResponse convertToResponse(Rank rank) {
        return RankResponse.builder()
                .rankId(rank.getRankId())
                .queueType(rank.getQueueType())
                .tier(rank.getTier())
                .rank(rank.getRank())
                .wins(rank.getWins())
                .losses(rank.getLosses())
                .winRate(rank.getWinRate())
                .gameAccountId(rank.getGameAccount().getGameAccountId())
                .createdAt(rank.getCreatedAt())
                .updatedAt(rank.getUpdatedAt())
                .build();
    }
}

