package com.back.matchduo.domain.post.repository;

import com.back.matchduo.domain.post.entity.GameMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameModeRepository extends JpaRepository<GameMode, Long> {

    // modeCode로 조회 (예: "SR")
    Optional<GameMode> findByModeCode(String modeCode);

    // 활성화된 게임 모드만 조회
    List<GameMode> findByIsActiveTrue();
}