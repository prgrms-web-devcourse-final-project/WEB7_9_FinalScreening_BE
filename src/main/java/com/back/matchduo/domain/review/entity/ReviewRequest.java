package com.back.matchduo.domain.review.entity;

import com.back.matchduo.domain.post.entity.Post;
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
                        name = "uk_review_request_post_user",
                        columnNames = {"post_id", "request_user_id"}
                )
        }
)
public class ReviewRequest extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 리뷰를 작성해야 하는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user_id", nullable = false)
    private User requestUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewRequestStatus status; // PENDING(모집완료/리뷰 불가능), COMPLETED(게임완료/리뷰가능), DONE(리뷰 모두 작성)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public ReviewRequest(Post post, User requestUser) {
        this.post = post;
        this.requestUser = requestUser;
        this.status = ReviewRequestStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusHours(6);
    }

    public void complete() {
        this.status = ReviewRequestStatus.COMPLETED;
    }

    public void done() {
        this.status = ReviewRequestStatus.DONE;
    }
}