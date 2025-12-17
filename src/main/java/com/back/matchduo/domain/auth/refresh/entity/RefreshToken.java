package com.back.matchduo.domain.auth.refresh.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "refresh_token",
        uniqueConstraints = {
                // 유저당 Refresh Token 1개 강제
                @UniqueConstraint(
                        name = "uk_refresh_token_user_id",
                        columnNames = "user_id"
                )
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 엔티티와 직접 연관관계 맺지 않음 (User 파트와 충돌 방지)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // JWT 문자열 길이 고려
    @Lob
    @Column(nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // 최초 저장용
    public static RefreshToken create(Long userId, String token, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.userId = userId;
        refreshToken.token = token;
        refreshToken.expiresAt = expiresAt;
        return refreshToken;
    }

    // 로그인 재시도 시 덮어쓰기
    public void update(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
}
