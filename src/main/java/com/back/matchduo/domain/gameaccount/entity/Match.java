package com.back.matchduo.domain.gameaccount.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_history",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"riot_match_id", "game_account_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "riot_match_id", nullable = false)
    private String riotMatchId;  // Riot API의 matchId (예: "KR_7929968207")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_account_id", referencedColumnName = "game_account_id", nullable = false)
    private GameAccount gameAccount;

    @Column(name = "queue_id", nullable = false)
    private Integer queueId;  // 420: 솔로랭크, 440: 자유랭크, 450: 칼바람 등

    @Column(name = "game_start_timestamp", nullable = false)
    private Long gameStartTimestamp;  // 밀리초

    @Column(name = "game_duration", nullable = false)
    private Integer gameDuration;  // 초

    @Column(name = "win", nullable = false)
    private Boolean win;  // 승리 여부

    @Builder
    public Match(String riotMatchId, GameAccount gameAccount, Integer queueId, 
                 Long gameStartTimestamp, Integer gameDuration, Boolean win) {
        this.riotMatchId = riotMatchId;
        this.gameAccount = gameAccount;
        this.queueId = queueId;
        this.gameStartTimestamp = gameStartTimestamp;
        this.gameDuration = gameDuration;
        this.win = win;
    }
}

