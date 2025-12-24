package com.back.matchduo.domain.user.repository;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    // 내가 차단한 유저 목록 조회 (최신순)
    List<UserBan> findAllByFromUserOrderByCreatedAtDesc(User fromUser);

    // 특정 유저 차단 여부 확인
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    // 차단 해제를 위한 단건 조회
    Optional<UserBan> findByFromUserAndToUser(User fromUser, User toUser);

    // 양방향 벤된 유저 ID 목록 조회 (내가 벤한 사람 + 나를 벤한 사람)
    @Query("SELECT DISTINCT CASE " +
            "WHEN ub.fromUser.id = :userId THEN ub.toUser.id " +
            "WHEN ub.toUser.id = :userId THEN ub.fromUser.id " +
            "END FROM UserBan ub " +
            "WHERE ub.fromUser.id = :userId OR ub.toUser.id = :userId")
    List<Long> findBannedUserIds(@Param("userId") Long userId);
}