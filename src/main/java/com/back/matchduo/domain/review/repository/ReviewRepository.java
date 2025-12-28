package com.back.matchduo.domain.review.repository;

import com.back.matchduo.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByPartyIdAndReviewerIdAndRevieweeId(Long partyId, Long reviewerId, Long revieweeId);

    Optional<Review> findByIdAndReviewerId(Long reviewId, Long reviewerId);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.reviewer m " +
            "WHERE r.reviewee.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByRevieweeId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.reviewee ee " +
            "WHERE r.reviewer.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByReviewerId(@Param("userId") Long userId);

    @Query("SELECT r.emoji, COUNT(r) " +
            "FROM Review r " +
            "WHERE r.reviewee.id = :userId " +
            "GROUP BY r.emoji")
    List<Object[]> countReviewEmojisByRevieweeId(@Param("userId") Long userId);

    long countByPartyIdAndReviewerId(Long partyId, Long userId);
}
