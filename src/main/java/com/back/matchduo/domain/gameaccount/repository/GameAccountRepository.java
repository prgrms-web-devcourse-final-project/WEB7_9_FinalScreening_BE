package com.back.matchduo.domain.gameaccount.repository;

import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    /**
     * 특정 유저의 특정 게임 타입 계정 조회
     * @param userId 유저 ID
     * @param gameType 게임 타입
     */
    Optional<GameAccount> findByUser_IdAndGameType(Long userId, String gameType);

    /**
     * 특정 유저의 모든 게임 계정 조회
     * @param userId 유저 ID
     * @return 게임 계정 목록
     */
    List<GameAccount> findByUser_Id(Long userId);
}
