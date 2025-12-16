package com.back.matchduo.domain.review.repository;

import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ReviewRequestRepository extends JpaRepository<ReviewRequest, Long> {

    Optional<ReviewRequest> findByPostIdAndRequestUserId(Long postId, Long userId);

    @Modifying
    @Query("DELETE FROM ReviewRequest rr WHERE rr.post.id = :postId")
    void deleteAllByPostIdHard(@Param("postId") Long postId);

    @Query("SELECT rr FROM ReviewRequest rr " +
            "JOIN FETCH rr.post p " +
            "WHERE rr.requestUser.id = :userId " +
            "AND rr.status = :status " +  // [핵심 변경] 상태 조건 추가
            "ORDER BY rr.createdAt DESC")
    List<ReviewRequest> findMyRequestsByStatus(
            @Param("userId") Long userId,
            @Param("status") ReviewRequestStatus status
    );
}
