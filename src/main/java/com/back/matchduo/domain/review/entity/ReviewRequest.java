package com.back.matchduo.domain.review.entity;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_request_party_user",
                        columnNames = {"party_id", "request_user_id"}
                )
        }
)
public class ReviewRequest extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    // 리뷰를 작성해야 하는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user_id", nullable = false)
    private User requestUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewRequestStatus status; // PENDING(모집완료/리뷰 불가능), COMPLETED(게임완료/리뷰가능)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public ReviewRequest(Party party, User requestUser) {
        this.party = party;
        this.requestUser = requestUser;
        this.status = ReviewRequestStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusHours(6);
    }

    public void complete() {
        this.status = ReviewRequestStatus.COMPLETED;
    }
}