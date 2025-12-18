package com.back.matchduo.domain.review.service;

import com.back.matchduo.domain.review.dto.response.ReviewRequestResponse;
import com.back.matchduo.domain.review.entity.ReviewRequest;
import com.back.matchduo.domain.review.enums.ReviewRequestStatus;
import com.back.matchduo.domain.review.repository.ReviewRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewRequestService {

    private final ReviewRequestRepository reviewRequestRepository;

    // 리뷰작성 가능한 리뷰요청관리 목록 조회
    public List<ReviewRequestResponse> getWritableReviewRequests(Long userId) {
        List<ReviewRequest> requests = reviewRequestRepository.findMyRequestsByStatus(
                userId,
                ReviewRequestStatus.COMPLETED
        );

        return requests.stream()
                .map(ReviewRequestResponse::from)
                .toList();
    }
}