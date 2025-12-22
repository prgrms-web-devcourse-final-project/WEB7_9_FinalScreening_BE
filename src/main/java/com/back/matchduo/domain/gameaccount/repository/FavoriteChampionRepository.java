package com.back.matchduo.domain.gameaccount.repository;

import com.back.matchduo.domain.gameaccount.entity.FavoriteChampion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteChampionRepository extends JpaRepository<FavoriteChampion, Long> {

    /**
     * 게임 계정의 선호 챔피언 조회 (순위 오름차순)
     */
    List<FavoriteChampion> findByGameAccount_GameAccountIdOrderByRankAsc(Long gameAccountId);

    /**
     * 게임 계정의 기존 선호 챔피언 삭제
     */
    void deleteByGameAccount_GameAccountId(Long gameAccountId);

    /**
     * 게임 계정과 순위로 선호 챔피언 조회
     */
    java.util.Optional<FavoriteChampion> findByGameAccount_GameAccountIdAndRank(Long gameAccountId, Integer rank);
}

