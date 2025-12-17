package com.back.matchduo.domain.gameaccount.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_rank")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rank extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id")
    private Long rankId;

    @Column(name = "queue_type", nullable = false)
    private String queueType; // RANKED_SOLO_5x5, RANKED_FLEX_SR

    @Column(name = "tier")
    private String tier; // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER

    @Column(name = "rank_division")
    private String rank; // I, II, III, IV

    @Column(name = "wins", nullable = false)
    private Integer wins;

    @Column(name = "losses", nullable = false)
    private Integer losses;

    @Column(name = "win_rate", nullable = false)
    private Double winRate; // 승률 (wins / (wins + losses) * 100)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_account_id", referencedColumnName = "game_account_id", nullable = false)
    private GameAccount gameAccount;

    @Builder
    public Rank(String queueType, String tier, String rank, Integer wins, Integer losses, Double winRate, GameAccount gameAccount) {
        this.queueType = queueType;
        this.tier = tier;
        this.rank = rank;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
        this.gameAccount = gameAccount;
    }

    /**
     * 랭크 정보 업데이트
     * @param tier 새로운 티어
     * @param rank 새로운 랭크
     * @param wins 새로운 승수
     * @param losses 새로운 패수
     * @param winRate 새로운 승률
     */
    public void update(String tier, String rank, Integer wins, Integer losses, Double winRate) {
        this.tier = tier;
        this.rank = rank;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
    }
}

