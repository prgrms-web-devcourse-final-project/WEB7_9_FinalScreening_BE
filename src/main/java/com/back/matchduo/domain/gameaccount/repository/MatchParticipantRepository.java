package com.back.matchduo.domain.gameaccount.repository;

import com.back.matchduo.domain.gameaccount.entity.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {

    /**
     * 매치 ID로 참가자 정보 조회
     */
    Optional<MatchParticipant> findByMatch_MatchId(Long matchId);

    /**
     * 게임 계정 ID로 모든 참가자 정보 삭제
     */
    void deleteByGameAccount_GameAccountId(Long gameAccountId);
}

