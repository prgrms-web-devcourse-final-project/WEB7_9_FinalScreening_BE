package com.back.matchduo.domain.gameaccount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /**
     * Riot 매치 상세 정보 응답
     * /lol/match/v5/matches/{matchId} 응답
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchResponse {
        @JsonProperty("metadata")
        private MatchMetadata metadata;

        @JsonProperty("info")
        private MatchInfo info;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MatchMetadata {
            @JsonProperty("dataVersion")
            private String dataVersion;

            @JsonProperty("matchId")
            private String matchId;

            @JsonProperty("participants")
            private List<String> participants;  // puuid 목록
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class MatchInfo {
            @JsonProperty("gameCreation")
            private Long gameCreation;

            @JsonProperty("gameDuration")
            private Integer gameDuration;

            @JsonProperty("gameEndTimestamp")
            private Long gameEndTimestamp;

            @JsonProperty("gameId")
            private Long gameId;

            @JsonProperty("gameMode")
            private String gameMode;

            @JsonProperty("gameName")
            private String gameName;

            @JsonProperty("gameStartTimestamp")
            private Long gameStartTimestamp;

            @JsonProperty("gameType")
            private String gameType;

            @JsonProperty("gameVersion")
            private String gameVersion;

            @JsonProperty("mapId")
            private Integer mapId;

            @JsonProperty("participants")
            private List<Participant> participants;

            @JsonProperty("platformId")
            private String platformId;

            @JsonProperty("queueId")
            private Integer queueId;

            @JsonProperty("teams")
            private List<Team> teams;

            @JsonProperty("tournamentCode")
            private String tournamentCode;
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Participant {
            @JsonProperty("assists")
            private Integer assists;

            @JsonProperty("champLevel")
            private Integer champLevel;

            @JsonProperty("championId")
            private Integer championId;

            @JsonProperty("championName")
            private String championName;

            @JsonProperty("deaths")
            private Integer deaths;

            @JsonProperty("item0")
            private Integer item0;

            @JsonProperty("item1")
            private Integer item1;

            @JsonProperty("item2")
            private Integer item2;

            @JsonProperty("item3")
            private Integer item3;

            @JsonProperty("item4")
            private Integer item4;

            @JsonProperty("item5")
            private Integer item5;

            @JsonProperty("item6")
            private Integer item6;

            @JsonProperty("kills")
            private Integer kills;

            @JsonProperty("perks")
            private Perks perks;

            @JsonProperty("puuid")
            private String puuid;

            @JsonProperty("summoner1Id")
            private Integer summoner1Id;

            @JsonProperty("summoner2Id")
            private Integer summoner2Id;

            @JsonProperty("teamId")
            private Integer teamId;

            @JsonProperty("teamPosition")
            private String teamPosition;

            @JsonProperty("totalMinionsKilled")
            private Integer totalMinionsKilled;

            @JsonProperty("win")
            private Boolean win;

            @Getter
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            public static class Perks {
                @JsonProperty("statPerks")
                private StatPerks statPerks;

                @JsonProperty("styles")
                private List<PerkStyle> styles;

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                public static class StatPerks {
                    @JsonProperty("defense")
                    private Integer defense;

                    @JsonProperty("flex")
                    private Integer flex;

                    @JsonProperty("offense")
                    private Integer offense;
                }

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                @Builder
                public static class PerkStyle {
                    @JsonProperty("description")
                    private String description;

                    @JsonProperty("selections")
                    private List<PerkSelection> selections;

                    @JsonProperty("style")
                    private Integer style;

                    @Getter
                    @NoArgsConstructor
                    @AllArgsConstructor
                    @Builder
                    public static class PerkSelection {
                        @JsonProperty("perk")
                        private Integer perk;

                        @JsonProperty("var1")
                        private Integer var1;

                        @JsonProperty("var2")
                        private Integer var2;

                        @JsonProperty("var3")
                        private Integer var3;
                    }
                }
            }
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Team {
            @JsonProperty("teamId")
            private Integer teamId;

            @JsonProperty("win")
            private Boolean win;
        }
    }
}

