package com.back.matchduo.domain.gameaccount.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_champion",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"game_account_id", "champion_rank"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteChampion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_champion_id")
    private Long favoriteChampionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_account_id", referencedColumnName = "game_account_id", nullable = false)
    private GameAccount gameAccount;

    @Column(name = "champion_rank", nullable = false)
    private Integer rank;  // 1, 2, 3

    @Column(name = "champion_id", nullable = false)
    private Integer championId;

    @Column(name = "champion_name", nullable = false)
    private String championName;

    @Column(name = "total_games", nullable = false)
    private Integer totalGames;  // 최근 20게임 중 사용 횟수

    @Column(name = "wins", nullable = false)
    private Integer wins;

    @Column(name = "losses", nullable = false)
    private Integer losses;

    @Column(name = "win_rate", nullable = false)
    private Double winRate;  // 승률 (%)

    @Builder
    public FavoriteChampion(GameAccount gameAccount, Integer rank, Integer championId,
                           String championName, Integer totalGames, Integer wins,
                           Integer losses, Double winRate) {
        this.gameAccount = gameAccount;
        this.rank = rank;
        this.championId = championId;
        this.championName = championName;
        this.totalGames = totalGames;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
    }

    /**
     * 선호 챔피언 정보 업데이트
     */
    public void update(Integer championId, String championName, Integer totalGames,
                      Integer wins, Integer losses, Double winRate) {
        this.championId = championId;
        this.championName = championName;
        this.totalGames = totalGames;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
    }
}

