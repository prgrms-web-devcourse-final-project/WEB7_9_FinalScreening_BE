package com.back.matchduo.domain.usersearch.repository;

import com.back.matchduo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserSearchQueryRepository {

    private final EntityManager em;

    public List<User> findUsers(String nickname, Long cursor, int limit) {
        String jpql =
                "SELECT u FROM User u " +
                        "WHERE u.nickname LIKE :keyword " +
                        (cursor != null ? "AND u.id < :cursor " : "") +
                        "ORDER BY u.id DESC";

        var query = em.createQuery(jpql, User.class)
                .setParameter("keyword", "%" + nickname + "%")
                .setMaxResults(limit);

        if (cursor != null) {
            query.setParameter("cursor", cursor);
        }

        return query.getResultList();
    }

    public long countUsers(String nickname) {
        return em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.nickname LIKE :keyword",
                        Long.class
                )
                .setParameter("keyword", "%" + nickname + "%")
                .getSingleResult();
    }
}
