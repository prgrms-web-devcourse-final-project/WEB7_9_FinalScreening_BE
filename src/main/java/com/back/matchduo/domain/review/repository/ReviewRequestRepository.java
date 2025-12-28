package com.back.matchduo.domain.review.repository;

import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ReviewRequestRepository extends JpaRepository<ReviewRequest, Long> {

    Optional<ReviewRequest> findByPartyIdAndRequestUserId(Long partyId, Long userId);

    List<ReviewRequest> findByRequestUserIdAndStatusAndIsActiveTrue(Long userId, ReviewRequestStatus reviewRequestStatus);

    @Modifying
    @Query("DELETE FROM ReviewRequest r WHERE r.party.id = :partyId AND r.status = :status")
    void deleteByPartyIdAndStatus(Long partyId, ReviewRequestStatus reviewRequestStatus);

    boolean existsByPartyId(Long partyId);

    List<ReviewRequest> findAllByPartyIdAndStatus(Long partyId, ReviewRequestStatus reviewRequestStatus);
}
