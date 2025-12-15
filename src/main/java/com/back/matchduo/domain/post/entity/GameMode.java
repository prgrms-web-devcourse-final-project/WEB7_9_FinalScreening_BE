package com.back.matchduo.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
@Table(
    name = "game_mode",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mode_code"})
    }
)
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_mode_id")
    private Long id;

    @Column(name = "mode_code", length = 20, nullable = false)
    private String modeCode;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GameMode(String modeCode, String name, Boolean isActive) {
        this.modeCode = modeCode;
        this.name = name;
        this.isActive = isActive;
    }

    public void updateActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }
}
