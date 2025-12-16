package com.back.matchduo.domain.review.dto.response;

public record ReviewDistributionResponse(
    Long goodCount,
    Long normalCount,
    Long badCount,
    Long totalCount
) {
    public static ReviewDistributionResponse of(long good, long normal, long bad) {
        return new ReviewDistributionResponse(good, normal, bad, good + normal + bad);
    }
}