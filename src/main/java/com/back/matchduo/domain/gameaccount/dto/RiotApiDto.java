package com.back.matchduo.domain.gameaccount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Riot API 응답 DTO
 */
public class RiotApiDto {

    /**
     * Riot 계정 정보 응답
     * /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine} 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountResponse {
        @JsonProperty("puuid")
        private String puuid;

        @JsonProperty("gameName")
        private String gameName;

        @JsonProperty("tagLine")
        private String tagLine;
    }

    /**
     * Riot 랭크 정보 응답
     * League API 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankResponse {
        @JsonProperty("leagueId")
        private String leagueId;

        @JsonProperty("queueType")
        private String queueType; // RANKED_SOLO_5x5, RANKED_FLEX_SR

        @JsonProperty("tier")
        private String tier; // IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER, CHALLENGER

        @JsonProperty("rank")
        private String rank; // I, II, III, IV

        @JsonProperty("puuid")
        private String puuid;

        @JsonProperty("leaguePoints")
        private Integer leaguePoints;

        @JsonProperty("wins")
        private Integer wins;

        @JsonProperty("losses")
        private Integer losses;

        @JsonProperty("veteran")
        private Boolean veteran;

        @JsonProperty("inactive")
        private Boolean inactive;

        @JsonProperty("freshBlood")
        private Boolean freshBlood;

        @JsonProperty("hotStreak")
        private Boolean hotStreak;
    }

    /**
     * Riot 소환사 정보 응답
     * /lol/summoner/v4/summoners/by-puuid/{puuid} 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummonerResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("accountId")
        private String accountId;

        @JsonProperty("puuid")
        private String puuid;

        @JsonProperty("name")
        private String name;

        @JsonProperty("profileIconId")
        private Integer profileIconId;

        @JsonProperty("revisionDate")
        private Long revisionDate;

        @JsonProperty("summonerLevel")
        private Long summonerLevel;
    }
}

