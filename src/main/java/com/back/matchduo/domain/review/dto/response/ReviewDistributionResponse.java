package com.back.matchduo.domain.review.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReviewDistributionResponse(
        Long userId,
        String nickname,
        Long totalReviews,
        Distribution distribution,
        Ratios ratios
) {
    public static ReviewDistributionResponse of(Long userId, String nickname, long good, long normal, long bad) {
        long total = good + normal + bad;

        if (total == 0) {
            return new ReviewDistributionResponse(
                    userId,
                    nickname,
                    0L,
                    new Distribution(0L, 0L, 0L),
                    new Ratios(0.0, 0.0, 0.0)
            );
        }

        double goodRatio = calculatePercentage(good, total);
        double normalRatio = calculatePercentage(normal, total);
        double badRatio = calculatePercentage(bad, total);

        return new ReviewDistributionResponse(
                userId,
                nickname,
                total,
                new Distribution(good, normal, bad),
                new Ratios(goodRatio, normalRatio, badRatio)
        );
    }

    private static double calculatePercentage(long count, long total) {
        double ratio = (count / (double) total) * 100;
        return Math.round(ratio * 10) / 10.0;
    }

    public record Distribution(
            @JsonProperty("GOOD") long good,
            @JsonProperty("NORMAL") long normal,
            @JsonProperty("BAD") long bad
    ) {}

    public record Ratios(
            @JsonProperty("GOOD") double good,
            @JsonProperty("NORMAL") double normal,
            @JsonProperty("BAD") double bad
    ) {}
}