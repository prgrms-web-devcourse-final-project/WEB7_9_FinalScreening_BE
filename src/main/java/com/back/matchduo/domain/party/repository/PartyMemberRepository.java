package com.back.matchduo.domain.party.repository;

import com.back.matchduo.domain.party.entity.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    // 1. 특정 파티의 멤버 목록 조회 (가입된 상태만 가져오거나, 전체 다 가져오거나)
    List<PartyMember> findByPartyId(Long partyId);

    // 2. 내가 참여한 파티 목록 조회
    List<PartyMember> findByUserId(Long userId);

    // 3. 이미 참여했는지 확인 (중복 참여 방지)
    boolean existsByPartyIdAndUserId(Long partyId, Long userId);

    // 4. 특정 파티에서 내 멤버 정보 찾기
    Optional<PartyMember> findByPartyIdAndUserId(Long partyId, Long userId);

    @Query("SELECT pm FROM PartyMember pm JOIN FETCH pm.user WHERE pm.party.id = :partyId AND pm.state = 'JOINED'")
    List<PartyMember> findActiveMembersByPartyId(@Param("partyId") Long partyId);

    // 내 파티 목록 조회 (Party 정보까지 Fetch Join)
    // 최신순(joinedAt 내림차순)으로 정렬
    @Query("SELECT pm FROM PartyMember pm JOIN FETCH pm.party WHERE pm.user.id = :userId ORDER BY pm.joinedAt DESC")
    List<PartyMember> findAllByUserIdWithParty(@Param("userId") Long userId);
}