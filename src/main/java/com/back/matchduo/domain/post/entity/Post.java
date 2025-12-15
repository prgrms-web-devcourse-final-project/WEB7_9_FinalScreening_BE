package com.back.matchduo.domain.post.entity;

import com.back.matchduo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.RECRUITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false, length = 20)
    private QueueType queueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "my_position", nullable = false, length = 20)
    private Position myPosition;

    @Column(name = "looking_positions", columnDefinition = "JSON", nullable = false)
    private String lookingPositions;

    @Column(nullable = false)
    private Boolean mic;

    @Column(name = "recruit_count", nullable = false)
    private Integer recruitCount;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Post(User user, GameMode gameMode, QueueType queueType, Position myPosition,
                String lookingPositions, Boolean mic, Integer recruitCount, String memo) {
        this.user = user;
        this.gameMode = gameMode;
        this.queueType = queueType;
        this.myPosition = myPosition;
        this.lookingPositions = lookingPositions;
        this.mic = mic;
        this.recruitCount = recruitCount;
        this.memo = memo;
        this.status = PostStatus.RECRUITING;
    }

    public void update(Position myPosition, String lookingPositions, QueueType queueType,
                       Boolean mic, Integer recruitCount, String memo) {
        if (myPosition != null) {
            this.myPosition = myPosition;
        }
        if (lookingPositions != null) {
            this.lookingPositions = lookingPositions;
        }
        if (queueType != null) {
            this.queueType = queueType;
        }
        if (mic != null) {
            this.mic = mic;
        }
        if (recruitCount != null) {
            this.recruitCount = recruitCount;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }

    public void updateStatus(PostStatus status) {
        this.status = status;
    }
}
