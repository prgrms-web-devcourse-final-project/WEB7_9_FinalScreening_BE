package com.back.matchduo.domain.post.repository;

import com.back.matchduo.domain.post.entity.Position;
import com.back.matchduo.domain.post.entity.Post;
import com.back.matchduo.domain.post.entity.PostStatus;
import com.back.matchduo.domain.post.entity.QueueType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostListQueryRepository {

    private final EntityManager em;

    // 목록 조회: 필터 + cursor + FINISHED 제외
    public List<Post> findPosts(
            Long cursor,
            int limitPlusOne,
            PostStatus status,
            QueueType queueType,
            Long gameModeId,
            List<Position> myPositions, // enum list
            String tier                 // "DIAMOND" 등
    ) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT DISTINCT p FROM Post p ");
        jpql.append("JOIN FETCH p.gameMode gm ");
        jpql.append("JOIN FETCH p.user u ");

        // tier 필터가 있으면 GameAccount/Rank join
        if (tier != null && !tier.isBlank()) {
            jpql.append("JOIN GameAccount ga ON ga.user = u ");
            jpql.append("JOIN Rank r ON r.gameAccount = ga ");
        }

        jpql.append("WHERE p.isActive = true ");
        jpql.append("AND p.status <> :finished ");

        if (cursor != null) {
            jpql.append("AND p.id < :cursor ");
        }
        if (status != null) {
            jpql.append("AND p.status = :status ");
        }
        if (queueType != null) {
            jpql.append("AND p.queueType = :queueType ");
        }
        if (gameModeId != null) {
            jpql.append("AND gm.id = :gameModeId ");
        }

        // myPositions 필터: ANY 포함되면 적용 안 함
        if (myPositions != null && !myPositions.isEmpty()) {
            jpql.append("AND p.myPosition IN :myPositions ");
        }

        // tier 필터 : 게임모드/큐타입 상관없이 "솔로랭크(RANKED_SOLO_5x5)" 기준으로만 tier 체크
        if (tier != null && !tier.isBlank()) {
            jpql.append("AND ga.gameType = :lolType ");
            jpql.append("AND r.queueType = :soloQueueType ");
            jpql.append("AND r.tier = :tier ");
        }

        jpql.append("ORDER BY p.id DESC");

        TypedQuery<Post> query = em.createQuery(jpql.toString(), Post.class);
        query.setParameter("finished", PostStatus.FINISHED);

        if (cursor != null) query.setParameter("cursor", cursor);
        if (status != null) query.setParameter("status", status);
        if (queueType != null) query.setParameter("queueType", queueType);
        if (gameModeId != null) query.setParameter("gameModeId", gameModeId);

        if (myPositions != null && !myPositions.isEmpty()) {
            query.setParameter("myPositions", myPositions);
        }

        if (tier != null && !tier.isBlank()) {
            query.setParameter("lolType", "LEAGUE_OF_LEGENDS");
            query.setParameter("soloQueueType", "RANKED_SOLO_5x5");
            query.setParameter("tier", tier);
        }

        query.setMaxResults(limitPlusOne);
        return query.getResultList();
    }

    public List<Position> parseMyPositionsCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        String[] tokens = csv.split(",");
        List<Position> list = new ArrayList<>();

        for (String t : tokens) {
            String v = t.trim();
            if (v.isEmpty()) continue;

            // ANY가 포함되면 필터 미적용
            if ("ANY".equalsIgnoreCase(v)) {
                return List.of();
            }

            // 잘못된 값이 오면 무시하지 말고 예외로 막고 싶으면 여기서 CustomException 던져도 됨
            // 현재는 값이 틀리면 400
            list.add(Position.valueOf(v.toUpperCase()));
        }

        return list;
    }
}
