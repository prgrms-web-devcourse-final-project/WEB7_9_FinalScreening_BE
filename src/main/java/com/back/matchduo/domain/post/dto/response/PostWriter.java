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
            Double winRate,              // placeholder
            Double kda,                  // placeholder
            java.util.List<String> favoriteChampions // placeholder
    ) {
    }
}
