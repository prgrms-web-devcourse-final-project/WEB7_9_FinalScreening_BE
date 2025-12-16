package com.back.matchduo.domain.gameaccount.repository;

import com.back.matchduo.domain.gameaccount.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, Long> {

    /**
     * 특정 게임 계정의 모든 랭크 정보 조회
     * @param gameAccountId 게임 계정 ID
     * @return 랭크 정보 목록
     */
    List<Rank> findByGameAccount_GameAccountId(Long gameAccountId);

    /**
     * 특정 게임 계정의 특정 큐 타입 랭크 정보 조회
     * @param gameAccountId 게임 계정 ID
     * @param queueType 큐 타입 (RANKED_SOLO_5x5, RANKED_FLEX_SR)
     * @return 랭크 정보
     */
    Optional<Rank> findByGameAccount_GameAccountIdAndQueueType(Long gameAccountId, String queueType);
}

