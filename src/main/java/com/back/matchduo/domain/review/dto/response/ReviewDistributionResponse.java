package com.back.matchduo.domain.review.dto.response;

public record ReviewDistributionResponse(
        Long goodCount,
        Long normalCount,
        Long badCount,
        Long totalCount,
        Double goodRatio,
        Double normalRatio,
        Double badRatio
) {
    public static ReviewDistributionResponse of(long good, long normal, long bad) {
        long total = good + normal + bad;

        if (total == 0) {
            return new ReviewDistributionResponse(0L, 0L, 0L, 0L, 0.0, 0.0, 0.0);
        }

        return new ReviewDistributionResponse(
                good,
                normal,
                bad,
                total,
                calculatePercentage(good, total),
                calculatePercentage(normal, total),
                calculatePercentage(bad, total)
        );
    }

    private static double calculatePercentage(long count, long total) {
        double ratio = (count / (double) total) * 100;
        return Math.round(ratio * 10) / 10.0;
    }
}