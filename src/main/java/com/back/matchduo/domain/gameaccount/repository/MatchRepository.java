package com.back.matchduo.domain.gameaccount.repository;

import com.back.matchduo.domain.gameaccount.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * 게임 계정의 최근 매치 목록 조회 (최신순)
     */
    List<Match> findByGameAccount_GameAccountIdOrderByGameStartTimestampDesc(Long gameAccountId);

    /**
     * 매치 ID와 게임 계정 ID로 매치 존재 여부 확인 (중복 체크)
     */
    boolean existsByRiotMatchIdAndGameAccount_GameAccountId(String riotMatchId, Long gameAccountId);

    /**
     * 매치 ID와 게임 계정 ID로 매치 조회
     */
    Optional<Match> findByRiotMatchIdAndGameAccount_GameAccountId(String riotMatchId, Long gameAccountId);

    /**
     * 게임 계정 ID로 모든 매치 삭제
     */
    void deleteByGameAccount_GameAccountId(Long gameAccountId);
}

