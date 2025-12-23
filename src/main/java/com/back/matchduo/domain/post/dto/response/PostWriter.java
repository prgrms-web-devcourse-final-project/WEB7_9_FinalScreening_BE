package com.back.matchduo.domain.post.dto.response;

public record PostWriter(
        Long userId,
        String communityNickname,
        String communityProfileImageUrl,
        WriterGameAccount gameAccount,
        WriterGameSummary gameSummary
) {
    public record WriterGameAccount(
            String gameType,
            String gameNickname,
            String gameTag,
            String profileIconUrl
    ) {
    }

    public record WriterGameSummary(
            String tier,
            String division,
            Double winRate,
            Double kda,
            Double avgKills, // 피그마 화면에 맞추어서 추가
            Double avgDeaths,
            Double avgAssists,
            java.util.List<String> favoriteChampions
    ) {
    }
}
