package com.back.matchduo.domain.usersearch.repository;

import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserSearchGameAccountQueryRepository {

    private final EntityManager em;

    public List<GameAccount> findLolAccountsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT ga FROM GameAccount ga " +
                                "JOIN FETCH ga.user u " +
                                "WHERE u.id IN :ids AND ga.gameType = 'LEAGUE_OF_LEGENDS'",
                        GameAccount.class
                )
                .setParameter("ids", userIds)
                .getResultList();
    }
}
