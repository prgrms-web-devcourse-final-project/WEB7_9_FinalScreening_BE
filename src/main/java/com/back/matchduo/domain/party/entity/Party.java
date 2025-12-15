package com.back.matchduo.domain.party.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "party")
public class Party {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 자동 파티완료 된 경우
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // 수동 파티완료, 자동 파티완료 된 경우의 시간
    @Column(name = "closed_at")
    private LocalDateTime closedAt;


    public Party(Long postId, Long leaderId) {
        this.postId = postId;
        this.leaderId = leaderId;
        this.status = PartyStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(6);
        }


    // 파티장이 수동 파티완료 처리
    public void closeParty() {
        if (this.status == PartyStatus.ACTIVE) {
            this.status = PartyStatus.CLOSED;
            this.closedAt = LocalDateTime.now(); // 현재 시간 기록
        }
    }

    // 스케줄러가 자동 파티완료 처리
    public void expireParty() {
        if (this.status == PartyStatus.ACTIVE) {
            this.status = PartyStatus.CLOSED;
            this.closedAt = this.expiresAt; // 만료 예정 시간이 곧 종료 시간
        }
    }
}
