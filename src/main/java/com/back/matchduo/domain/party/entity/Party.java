package com.back.matchduo.domain.party.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "party")
public class Party extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "party_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyStatus status;


    // 자동 파티완료 된 경우
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // 수동 파티완료, 자동 파티완료 된 경우의 시간
    @Column(name = "closed_at")
    private LocalDateTime closedAt;


    // 파티 초기 상태
    public Party(Long postId, Long leaderId) {
        this.postId = postId;
        this.leaderId = leaderId;
        this.status = PartyStatus.RECRUIT;
        this.expiresAt = null;
        }

    // 모집 완료 -> ACTIVE 전환
    public void activateParty(LocalDateTime expiresAt) {
        this.status = PartyStatus.ACTIVE;
        this.expiresAt = expiresAt;
    }

    // 3. 멤버 이탈 -> RECRUIT 복귀
    public void downgradeToRecruit() {
        if (this.status == PartyStatus.ACTIVE) {
            this.status = PartyStatus.RECRUIT;
            this.expiresAt = null;
        }
    }

    // 파티장이 수동 파티완료 처리
    public void closeParty() {
        if (this.status == PartyStatus.ACTIVE || this.status == PartyStatus.RECRUIT) {
            this.status = PartyStatus.CLOSED;
            this.closedAt = LocalDateTime.now();
        }
    }

    // 스케줄러가 자동 파티완료 처리
    public void expireParty() {
        if (this.status == PartyStatus.ACTIVE) {
            this.status = PartyStatus.CLOSED;
            this.closedAt = this.expiresAt;
        }
    }
}
