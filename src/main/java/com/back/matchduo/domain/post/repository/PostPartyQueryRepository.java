package com.back.matchduo.domain.post.repository;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.party.entity.PartyMember;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostPartyQueryRepository {

    private final EntityManager em;

    public List<Party> findPartiesByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT p FROM Party p WHERE p.postId IN :postIds",
                        Party.class
                )
                .setParameter("postIds", postIds)
                .getResultList();
    }

    public List<PartyMember> findJoinedMembersByPartyIds(List<Long> partyIds) {
        if (partyIds == null || partyIds.isEmpty()) return List.of();

        // PartyMemberRepository의 findActiveMembersByPartyId 를 "IN 버전"으로 Post에서 구현
        return em.createQuery(
                        "SELECT pm FROM PartyMember pm " +
                                "JOIN FETCH pm.user u " +
                                "WHERE pm.party.id IN :partyIds AND pm.state = 'JOINED'",
                        PartyMember.class
                )
                .setParameter("partyIds", partyIds)
                .getResultList();
    }
}
