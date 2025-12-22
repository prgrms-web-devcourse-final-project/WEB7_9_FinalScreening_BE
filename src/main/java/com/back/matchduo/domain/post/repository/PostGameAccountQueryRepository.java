package com.back.matchduo.domain.post.repository;

import com.back.matchduo.domain.gameaccount.entity.FavoriteChampion;
import com.back.matchduo.domain.gameaccount.entity.GameAccount;
import com.back.matchduo.domain.gameaccount.entity.MatchParticipant;
import com.back.matchduo.domain.gameaccount.entity.Rank;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostGameAccountQueryRepository {

    private final EntityManager em;

    public List<GameAccount> findLolAccountsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT ga FROM GameAccount ga " +
                                "JOIN FETCH ga.user u " +
                                "WHERE u.id IN :userIds AND ga.gameType = :gameType",
                        GameAccount.class
                )
                .setParameter("userIds", userIds)
                .setParameter("gameType", "LEAGUE_OF_LEGENDS")
                .getResultList();
    }

    public List<Rank> findRanksByGameAccountIds(List<Long> gameAccountIds) {
        if (gameAccountIds == null || gameAccountIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT r FROM Rank r " +
                                "JOIN FETCH r.gameAccount ga " +
                                "WHERE ga.gameAccountId IN :ids",
                        Rank.class
                )
                .setParameter("ids", gameAccountIds)
                .getResultList();
    }

    public List<MatchParticipant> findRecentSoloRankMatchParticipantsByGameAccountIds(
            List<Long> gameAccountIds, int limit) {
        if (gameAccountIds == null || gameAccountIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT mp FROM MatchParticipant mp " +
                                "JOIN FETCH mp.match m " +
                                "WHERE mp.gameAccount.gameAccountId IN :ids " +
                                "AND m.queueId = 420 " +
                                "ORDER BY m.gameStartTimestamp DESC",
                        MatchParticipant.class
                )
                .setParameter("ids", gameAccountIds)
                .setMaxResults(gameAccountIds.size() * limit)
                .getResultList();
    }

    public List<FavoriteChampion> findFavoriteChampionsByGameAccountIds(List<Long> gameAccountIds) {
        if (gameAccountIds == null || gameAccountIds.isEmpty()) return List.of();

        return em.createQuery(
                        "SELECT fc FROM FavoriteChampion fc " +
                                "WHERE fc.gameAccount.gameAccountId IN :ids " +
                                "ORDER BY fc.gameAccount.gameAccountId ASC, fc.rank ASC",
                        FavoriteChampion.class
                )
                .setParameter("ids", gameAccountIds)
                .getResultList();
    }
}
