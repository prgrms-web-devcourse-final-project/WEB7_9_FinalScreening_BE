package com.back.matchduo.domain.party.repository;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {

    // 1. 모집글 ID로 파티 조회
    // 용도: GET /api/v1/posts/{postId}/party (모집글 눌렀을 때 파티 정보 띄우기)
    Optional<Party> findByPostId(Long postId);

    // 2. 만료 시간이 지났고, 상태가 ACTIVE인 파티 조회
    // 용도: 스케줄러가 6시간 지난 파티를 찾아서 자동으로 닫을 때 사용
    List<Party> findByStatusAndExpiresAtBefore(PartyStatus status, LocalDateTime now);
}
