package com.back.matchduo.domain.user.repository;

import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.domain.user.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    // 내가 차단한 유저 목록 조회 (최신순)
    List<UserBan> findAllByFromUserOrderByCreatedAtDesc(User fromUser);

    // 특정 유저 차단 여부 확인
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    // 차단 해제를 위한 단건 조회
    Optional<UserBan> findByFromUserAndToUser(User fromUser, User toUser);
}