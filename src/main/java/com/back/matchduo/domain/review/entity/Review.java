package com.back.matchduo.domain.review.entity;

import com.back.matchduo.domain.party.entity.Party;
import com.back.matchduo.domain.review.enums.ReviewEmoji;
import com.back.matchduo.domain.user.entity.User;
import com.back.matchduo.global.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_active = true")
@Table(
        name = "review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_reviewer_reviewee_party",
                        columnNames = {"reviewer_id", "reviewee_id", "party_id"}
                )
        }
)
public class Review extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    // 요청 데이터가 삭제돼도 리뷰는 남아야 하므로 nullable = true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_request_id", nullable = true)
    private ReviewRequest reviewRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewEmoji emoji;

    @Column(length = 100)
    private String content = "";

    @Builder
    public Review(Party party, User reviewer, User reviewee, ReviewRequest reviewRequest, ReviewEmoji emoji, String content) {
        validateSelfReview(reviewer, reviewee);
        this.party = party;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.reviewRequest = reviewRequest;
        this.emoji = emoji;
        this.content = content;
    }

    public void update(ReviewEmoji emoji, String content) {
        this.emoji = emoji;
        this.content = content;
    }

    private void validateSelfReview(User reviewer, User reviewee) {
        if (reviewer.getId().equals(reviewee.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 리뷰를 작성할 수 없습니다.");
        }
    }
}