package com.back.matchduo.domain.gameaccount.entity;

import com.back.matchduo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_participant", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"match_id", "game_account_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_participant_id")
    private Long matchParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", referencedColumnName = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_account_id", referencedColumnName = "game_account_id", nullable = false)
    private GameAccount gameAccount;

    @Column(name = "champion_id", nullable = false)
    private Integer championId;

    @Column(name = "champion_name", nullable = false)
    private String championName;

    @Column(name = "spell1_id", nullable = false)
    private Integer spell1Id;

    @Column(name = "spell2_id", nullable = false)
    private Integer spell2Id;

    @Column(name = "kills", nullable = false)
    private Integer kills;

    @Column(name = "deaths", nullable = false)
    private Integer deaths;

    @Column(name = "assists", nullable = false)
    private Integer assists;

    @Column(name = "kda", nullable = false)
    private Double kda;

    @Column(name = "cs", nullable = false)
    private Integer cs;  // totalMinionsKilled

    @Column(name = "level", nullable = false)
    private Integer level;  // champLevel

    @Column(name = "item0")
    private Integer item0;

    @Column(name = "item1")
    private Integer item1;

    @Column(name = "item2")
    private Integer item2;

    @Column(name = "item3")
    private Integer item3;

    @Column(name = "item4")
    private Integer item4;

    @Column(name = "item5")
    private Integer item5;

    @Column(name = "item6")
    private Integer item6;

    @Column(name = "perks", columnDefinition = "TEXT")
    private String perks;  // JSON 형태로 저장

    @Column(name = "puuid")
    private String puuid;  // 매치 참가 시 사용된 puuid (계정 수정 시 필터링용)

    @Builder
    public MatchParticipant(Match match, GameAccount gameAccount, Integer championId, 
                           String championName, Integer spell1Id, Integer spell2Id,
                           Integer kills, Integer deaths, Integer assists, Double kda,
                           Integer cs, Integer level, Integer item0, Integer item1,
                           Integer item2, Integer item3, Integer item4, Integer item5,
                           Integer item6, String perks, String puuid) {
        this.match = match;
        this.gameAccount = gameAccount;
        this.championId = championId;
        this.championName = championName;
        this.spell1Id = spell1Id;
        this.spell2Id = spell2Id;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.kda = kda;
        this.cs = cs;
        this.level = level;
        this.item0 = item0;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.item5 = item5;
        this.item6 = item6;
        this.perks = perks;
        this.puuid = puuid;
    }
}

